package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManager;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManagerFactory;
import org.jenkins.plugins.qualitytrends.model.QualityTrendsModule;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.Map;

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
    public Map<String, Integer> getSeverities() {
        if (storage == null) initStorage();
        return storage.getSeverities();
    }

    @JavaScriptMethod
    public Map<String, Integer> getPreviousSeverities() {
        if (storage == null) initStorage();
        return storage.getPreviousSeverities();
    }

    @JavaScriptMethod
    public JSONArray getParsers() {
        if (storage == null) initStorage();
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : storage.getParsers().entrySet()) {
            JSONObject json = new JSONObject()
                    .accumulate("parser", entry.getKey())
                    .accumulate("amount", entry.getValue());
            jsonArray.add(json);
        }
        return jsonArray;
    }

    @JavaScriptMethod
    public JSONArray getPreviousParsers() {
        if (storage == null) initStorage();
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : storage.getParsers().entrySet()) {
            JSONObject json = new JSONObject()
                    .accumulate("parser", entry.getKey())
                    .accumulate("amount", entry.getValue());
            jsonArray.add(json);
        }
        return jsonArray;
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
