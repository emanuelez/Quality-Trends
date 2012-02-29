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
        return new JSONObject()
                .element("infos", this.getInfos())
                .element("warnings", this.getWarnings())
                .element("errors", this.getErrors())
                .element("orphans", this.getOrphans())
                .element("infos_prev", this.getPrevErrors())
                .element("warnings_prev", this.getPrevWarnings())
                .element("errors_prev", this.getPrevErrors())
                .element("orphans_prev", this.getPrevOrphans());
    }


    public int getPrevInfos() {
        try {
            if (storage == null) initStorage();
            if (build.getPreviousSuccessfulBuild() == null) return 0;
            return storage.getInfos(build.getPreviousSuccessfulBuild());
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getPrevWarnings() {
        try {
            if (storage == null) initStorage();
            if (build.getPreviousSuccessfulBuild() == null) return 0;
            return storage.getWarnings(build.getPreviousSuccessfulBuild());
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getPrevErrors() {
        try {
            if (storage == null) initStorage();
            if (build.getPreviousSuccessfulBuild() == null) return 0;
            return storage.getErrors(build.getPreviousSuccessfulBuild());
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getPrevOrphans() {
        try {
            if (storage == null) initStorage();
            if (build.getPreviousSuccessfulBuild() == null) return 0;
            return storage.getOrphans(build.getPreviousSuccessfulBuild());
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getInfos() {
        try {
            if (storage == null) initStorage();
            return storage.getInfos();
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getWarnings() {
        try {
            if (storage == null) initStorage();
            return storage.getWarnings();
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getErrors() {
        try {
            if (storage == null) initStorage();
            return storage.getErrors();
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public int getOrphans() {
        try {
            if (storage == null) initStorage();
            return storage.getOrphans();
        } catch(Throwable t) {
            t.printStackTrace();
            return 0;
        }
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
