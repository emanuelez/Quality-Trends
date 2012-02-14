package org.jenkins.plugins.qualitytrends;

import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkins.plugins.qualitytrends.model.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
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
        info("Prebuild Started!");
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
        info("Prebuild Done!");
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        logger = listener.getLogger();
        
        info("Waiting for the entries to be stored...");
        while(!future.isDone()) {
            Thread.sleep(1000);
        }
        info("DONE.");

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

}
