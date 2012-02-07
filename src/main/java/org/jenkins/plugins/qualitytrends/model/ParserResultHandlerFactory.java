package org.jenkins.plugins.qualitytrends.model;

import hudson.model.AbstractBuild;

/**
 * @author Emanuele Zattin
 */
public interface ParserResultHandlerFactory {
    public ParserResultHandler create(AbstractBuild build);
}
