package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jenkins.plugins.qualitytrends.model.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
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

    private Set<Parser> parsers;
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
        while(!future.isDone()) {
            Thread.sleep(1000);
        }
        info("Done");

        // Print info about the entries found
        if (!recapEntries(logger)) return false;

        // Find the files names associated to the entries
        Set<String> allFileNames;
        try {
            allFileNames = storage.getFileNames();
            info(allFileNames.size() + " paths found");
        } catch (QualityTrendsException e) {
            error("Could not retrieve the file names from the DB");
            e.printStackTrace();
            return false;
        }

        // Find the absolute paths
        Map<String,String> absolutePaths = build.getWorkspace().act(new TreeTraversalFileCallable(allFileNames, 500));
        info(absolutePaths.size() + " absolute paths were found after traversing the work area");

        // Copy the files to the Master
        if (!copyFilesToMaster(build, git, absolutePaths)) return false;

        return true;
    }

    private boolean copyFilesToMaster(AbstractBuild<?, ?> build, Git git, Map<String, String> absolutePaths) throws IOException, InterruptedException {
        VirtualChannel channel = build.getBuiltOn().getChannel();
        String workspacePath = build.getWorkspace().getRemote();
        for (String absolutePath : absolutePaths.values()) {
            // Get the relative path
            Preconditions.checkState(absolutePath.startsWith(workspacePath));
            String relativePath = absolutePath.substring(workspacePath.length());

            FilePath source = new FilePath(channel, absolutePath);

            File localPath = new File(build.getProject().getRootDir(), "QualityTrends" + relativePath);
            FilePath destination = new FilePath(localPath);
            destination.copyFrom(source);
        }
        File buildNumberFile = new File(build.getProject().getRootDir(), "QualityTrends" + "/.buildNumber");
        Files.write(Integer.toString(build.getNumber()), buildNumberFile, Charset.defaultCharset());
        try {
            git.add().addFilepattern(".").call();
        } catch (GitAPIException e) {
            error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        try {
            git.commit().setMessage(Integer.toString(build.getNumber())).call();
        } catch (GitAPIException e) {
            error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        info("Files copied to Master");
        return true;
    }

    private boolean recapEntries(PrintStream logger) {
        int entryNumber;
        try {
            entryNumber = storage.getEntryNumberForBuild();
        } catch (QualityTrendsException e) {
            error("Could not get the number of entries for this build from the DB");
            e.printStackTrace();
            return false;
        }
        info(MessageFormat.format("{0} entries were found.", entryNumber));

        for (Parser parser : parsers) {
            try {
                entryNumber = storage.getEntryNumberForBuildAndParser(parser);
            } catch (QualityTrendsException e) {
                logger.println("[QualityTrends] [ERROR] Could not get the number of entries for this build and this parser from the DB");
                e.printStackTrace();
                return false;
            }
            info(MessageFormat.format("{0} entries for the {1} parser", entryNumber, parser.getName()));
        }
        return true;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    public Iterable<Parser> getParsers() {
        return parsers;
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
