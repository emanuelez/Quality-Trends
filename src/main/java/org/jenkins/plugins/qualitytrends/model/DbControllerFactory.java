package org.jenkins.plugins.qualitytrends.model;

/**
 * @author Emanuele Zattin
 */

public interface DbControllerFactory {
    public DbController create(String path);
}
