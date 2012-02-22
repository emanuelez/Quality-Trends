package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Sets;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitUtils;
import org.jenkins.plugins.qualitytrends.model.*;
import org.jenkins.plugins.qualitytrends.util.Sha1Utils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Emanuele Zattin
 */
public class QualityTrends extends Recorder implements Serializable {

    private Set<Parser> parsers = Sets.newHashSet();
    private Future future;
    private BuildStorageManager storage;
    private transient PrintStream logger;


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public QualityTrends(Set<Parser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        logger = listener.getLogger();
        info("Prebuild initiated...");
        // Guice stuff
        Injector injector = Guice.createInjector(new QualityTrendsModule());
        try {
            BuildStorageManagerFactory buildStorageManagerFactory = injector.getInstance(BuildStorageManagerFactory.class);
            storage = buildStorageManagerFactory.create(build);
        } catch (Exception e) {
            e.printStackTrace();
            error("Could not create storage");
            return false;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new QualityTrendsRunnable(build, storage));
        info("Done");
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        logger = listener.getLogger();

        Initializer initializer = new Initializer(build).invoke();
        if (initializer.isFailed()) return false;
        Git git = initializer.getGit();

        info("Waiting for the entries to be stored...");
        while (!future.isDone()) {
            Thread.sleep(1000);
        }
        info("Done");

        try {
            // Print info about the entries found
            recapEntries();

            // Find the files names associated to the entries
            Set<String> allFileNames = storage.getFileNames();
            info(allFileNames.size() + " paths found");

            // Find the absolute paths
            BiMap<String, String> absolutePaths = build.getWorkspace().act(new TreeTraversalFileCallable(allFileNames, 500));
            info(absolutePaths.size() + " absolute paths were found after traversing the work area");

            // Copy the files to the Master
            copyFilesToMaster(build, git, absolutePaths);

            // Generate the warnings
            generateWarnings(git);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                // Be a good boy and let runtime exceptions go their way
                e.printStackTrace();
                return true;
            }
            error(e.getMessage());
            e.printStackTrace();
            return false;
        }

        build.addAction(new BuildAction(build));

        return true;
    }

    private boolean generateWarnings(Git git) throws NoSuchAlgorithmException, IOException, QualityTrendsException {
        info("Generating and storing warnings...");

        // Find the new (fileSha1,lineNumber) couples
        Map<String, Integer> couples = storage.getNewFileSha1AndLineNumber();

        // Compute the contextSha1 for the new (fileSha1,lineNumber) couples
        Map<Map.Entry<String, Integer>, String> contextSha1 = Maps.newHashMap();
        for (Map.Entry<String, Integer> couple : couples.entrySet()) {
            System.out.println(couple.getKey() + " -> " + couple.getValue());
            String sha1 = Sha1Utils.generateContextSha1(
                    BlobUtils.getContent(git.getRepository(), git.getRepository().resolve(couple.getKey())),
                    couple.getValue());
            contextSha1.put(couple, sha1);
        }

        // Compute the warningSha1 for each entry relative to each new (fileSha1,lineNumber) couple
        for (Map.Entry<Map.Entry<String, Integer>, String> contextEntry : contextSha1.entrySet()) {
            // Find all entries associated to the couple
            Set<Entry> entries = storage.findEntriesForFileSha1AndLineNumber(contextEntry.getKey().getKey(), contextEntry.getKey().getValue());
            for (Entry entry : entries) {
                String warningSha1 = Sha1Utils.generateWarningSha1(contextEntry.getValue(), entry.getParser(), entry.getSeverity().toString(), entry.getMessage());
                storage.addWarning(warningSha1, entry);
            }
        }
        info("Done");
        return true;
    }

    private void copyFilesToMaster(AbstractBuild<?, ?> build, Git git, BiMap<String, String> absolutePaths) throws IOException, InterruptedException, NoFilepatternException, NoHeadException, NoMessageException, ConcurrentRefUpdateException, WrongRepositoryStateException, QualityTrendsException {
        VirtualChannel channel = build.getBuiltOn().getChannel();
        String workspacePath = build.getWorkspace().getRemote();
        for (String absolutePath : absolutePaths.values()) {
            // Get the relative path
            Preconditions.checkState(absolutePath.startsWith(workspacePath));
            String relativePath = absolutePath.substring(workspacePath.length());
            // Copy from...
            FilePath source = new FilePath(channel, absolutePath);
            // Copy to...
            File localPath = new File(build.getProject().getRootDir(), "QualityTrends" + relativePath);
            FilePath destination = new FilePath(localPath);
            // Do it!
            destination.copyFrom(source);
        }
        // Update the buildNumber file
        File buildNumberFile = new File(build.getProject().getRootDir(), "QualityTrends" + "/.buildNumber");
        Files.write(Integer.toString(build.getNumber()), buildNumberFile, Charset.defaultCharset());
        // Add all the files
        git.add().addFilepattern(".").call();
        // Commit
        git.commit().setMessage(Integer.toString(build.getNumber())).call();
        // Get the new commit
        RevCommit currentCommit = CommitUtils.getHead(git.getRepository());

        for (Map.Entry<String, String> entry : absolutePaths.entrySet()) {
            String relativePath = entry.getValue().substring(workspacePath.length() + 1);
            ObjectId objectId = BlobUtils.getId(git.getRepository(), currentCommit.getId(), relativePath);
            if (objectId != null) {
                storage.updateEntryWithFileSha1(entry.getKey(), ObjectId.toString(objectId));
            }
        }
        info("Files copied to Master");
    }

    private void recapEntries() throws QualityTrendsException {
        int entryNumber = storage.getEntryNumberForBuild();
        info(MessageFormat.format("{0} entries were found.", entryNumber));

        for (Parser parser : parsers) {
            entryNumber = storage.getEntryNumberForBuildAndParser(parser);
            info(MessageFormat.format("{0} entries for the {1} parser", entryNumber, parser.getName()));
        }
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    public Set<Parser> getParsers() {
        if(parsers != null)
            return parsers;
        else
            return Sets.newHashSet();
    }

    private void log(String s) {
        logger.println("[QualityTrends] " + s);
    }

    private void info(String s) {
        log("[INFO] " + s);
    }

    private void warning(String s) {
        log("[WARNING] " + s);
    }

    private void error(String s) {
        log("[ERROR] " + s);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "Quality Trends";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            Set<Parser> parsers = Sets.newHashSet();

            for (Parser parser : Parser.all()) {
                if (formData.optBoolean(parser.getName())) {
                    parsers.add(parser);
                }
            }

            return new QualityTrends(parsers);
        }
    }

    private class Initializer {
        private boolean failed;
        private AbstractBuild<?, ?> build;
        private Git git;

        public Initializer(AbstractBuild<?, ?> build) {
            this.build = build;
        }

        boolean isFailed() {
            return failed;
        }

        public Git getGit() {
            return git;
        }

        public Initializer invoke() throws IOException {
            File gitFolder = new File(build.getProject().getRootDir(), "QualityTrends");
            if (!gitFolder.exists() && !gitFolder.isDirectory()) {
                gitFolder.mkdir();
            }
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            FileRepository repository = builder
                    .setWorkTree(gitFolder)
                    .findGitDir()
                    .build();
            git = new Git(repository);
            if (!(new File(gitFolder, ".git").isDirectory())) {
                info("QualityTrends was not initialized yet. Initializing...");
                git = Git.init().setDirectory(gitFolder).call();
                File f = new File(build.getProject().getRootDir(), "QualityTrends/.buildNumber");
                Files.write("0", f, Charset.defaultCharset());
                try {
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage("0").call();
                } catch (GitAPIException e) {
                    error(e.getMessage());
                    e.printStackTrace();
                    failed = true;
                    return this;
                }
                info("Done");
            }

            try {
                git.checkout().setName("master").call();
            } catch (GitAPIException e) {
                error(e.getMessage());
                e.printStackTrace();
                failed = true;
                return this;
            }
            failed = false;
            return this;
        }
    }
}
