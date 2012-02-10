package org.jenkins.plugins.qualitytrends.model;

import hudson.model.AbstractBuild;

/**
 * @author Emanuele Zattin
 */
public interface BuildStorageManagerFactory {
    public BuildStorageManager create(AbstractBuild build);
}
