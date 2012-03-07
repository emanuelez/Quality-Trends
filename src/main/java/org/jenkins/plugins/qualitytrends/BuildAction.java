package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import net.sf.json.JSONObject;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManager;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManagerFactory;
import org.jenkins.plugins.qualitytrends.model.QualityTrendsModule;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * @author Emanuele Zattin
 */

public class BuildAction implements Action {

    private AbstractBuild build;
    transient private BuildStorageManager storage;

    public BuildAction(AbstractBuild build) {
        this.build = build;

        initStorage();
    }

    private void initStorage() {
        Injector injector = Guice.createInjector(new QualityTrendsModule());
        try {
            BuildStorageManagerFactory buildStorageManagerFactory = injector.getInstance(BuildStorageManagerFactory.class);
            storage = buildStorageManagerFactory.create(build);
        } catch (Throwable t) {
            t.printStackTrace();
            Throwables.propagate(t);
        }
    }

    public AbstractBuild getBuild() {
        return build;
    }

    @JavaScriptMethod
    public JSONObject getSeverities() {
        if (storage == null) initStorage();
        return storage.getSeverities();
    }

    @JavaScriptMethod
    public JSONObject getPreviousSeverities() {
        if (storage == null) initStorage();
        return storage.getPreviousSeverities();
    }

    @JavaScriptMethod
    public JSONObject getParsers() {
        if (storage == null) initStorage();
        return storage.getParsers();
    }

    @JavaScriptMethod
    public JSONObject getPreviousParsers() {
        if (storage == null) initStorage();
        return storage.getPreviousParsers();
    }

    @JavaScriptMethod
    public int getPreviousOrphans() {
        if (storage == null) initStorage();
        if (build.getPreviousSuccessfulBuild() == null) return 0;
        return storage.getPreviousOrphans();
    }

    @JavaScriptMethod
    public int getOrphans() {
        if (storage == null) initStorage();
        return storage.getOrphans();
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
