package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManager;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManagerFactory;
import org.jenkins.plugins.qualitytrends.model.DbBuildStorageManager;
import org.jenkins.plugins.qualitytrends.model.QualityTrendsModule;

/**
 * @author Emanuele Zattin
 */

public class BuildAction implements Action {

    private AbstractBuild build;
    private BuildStorageManager storage;

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
