package org.jenkins.plugins.qualitytrends;

import hudson.model.AbstractBuild;
import hudson.model.Action;

/**
 * @author Emanuele Zattin
 */

public class BuildAction implements Action {

    private AbstractBuild build;

    public BuildAction(AbstractBuild build) {
        this.build = build;
    }

    public AbstractBuild getBuild() {
        return build;
    }

    public String getIconFileName() {
        return "/plugin/quality-trends/graph.png";
    }

    public String getDisplayName() {
        return "Quality Trends";
    }

    public String getUrlName() {
        return "qualitytrends";
    }
}
