package org.jenkins.plugins.qualitytrends.model;

import hudson.model.AbstractBuild;

/**
 * @author Emanuele Zattin
 */
public interface StorageManagerFactory {
    public StorageManager create(AbstractBuild build);
}
