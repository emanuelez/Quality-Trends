package org.jenkins.plugins.qualitytrends;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * @author Emanuele Zattin
 */
public class QualityTrends extends Recorder {

    private Iterable<Parser> parsers;

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public QualityTrends(Iterable<Parser> parsers) {
        this.parsers = parsers;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        // For every parser scan the console log
        for (Parser parser : parsers) {

        }


        return super.perform(build, launcher, listener);
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
