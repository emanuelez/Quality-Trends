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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Emanuele Zattin
 */
public class QualityTrends extends Recorder {

    private Iterable<Parser> parsers;
    private Future future;
    private ParserResultHandler handler;
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
            ParserResultHandlerFactory parserResultHandlerFactory = injector.getInstance(ParserResultHandlerFactory.class);
            handler = parserResultHandlerFactory.create(build);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new QualityTrendsRunnable(build, handler));
        System.out.println("Prebuild Done!");
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        while(!future.isDone()) {
            Thread.sleep(1000);
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
