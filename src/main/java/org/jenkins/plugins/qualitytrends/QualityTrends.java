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
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Emanuele Zattin
 */
public class QualityTrends extends Recorder {

    private Iterable<Parser> parsers;
    private Future future;
    private StorageManager storage;
    private Injector injector;

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public QualityTrends(Iterable<Parser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        System.out.println("Prebuild Started!");
        // Guice stuff
        injector = Guice.createInjector(new QualityTrendsModule());
        System.out.println("Prebuild Middle!");
        try {
            StorageManagerFactory storageManagerFactory = injector.getInstance(StorageManagerFactory.class);
            storage = storageManagerFactory.create(build);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new QualityTrendsRunnable(build, storage));
        System.out.println("Prebuild Done!");
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        final PrintStream logger = listener.getLogger();
        
        logger.println("[QualityTrends] Waiting for the entries to be stored...");
        while(!future.isDone()) {
            Thread.sleep(1000);
        }
        logger.println("[QualityTrends] DONE.");

        // Print info about the entries found
        if (!recapEntries(build, logger)) return false;

        // Find the files associated to the entries

        return true;
    }

    private boolean recapEntries(AbstractBuild<?, ?> build, PrintStream logger) {
        int entryNumber;
        try {
            entryNumber = storage.getEntryNumberForBuild(build);
        } catch (QualityTrendsException e) {
            logger.println("[QualityTrends] [ERROR] Could not get the number of entries for this build from the DB");
            e.printStackTrace();
            return false;
        }
        logger.println(MessageFormat.format("[QualityTrends] {0} entries were found.", entryNumber));

        for (Parser parser : parsers) {
            try {
                entryNumber = storage.getEntryNumberForBuildAndParser(build, parser);
            } catch (QualityTrendsException e) {
                logger.println("[QualityTrends] [ERROR] Could not get the number of entries for this build and this parser from the DB");
                e.printStackTrace();
                return false;
            }
            logger.println(MessageFormat.format("[QualityTrends] {0} entries for the {1} parser", entryNumber, parser.getName()));
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
