package org.jenkins.plugins.qualitytrends.model;

/**
 * @author Emanuele Zattin
 */
public interface StorageManager {

    abstract public void add(ParserResult parserResult) throws QualityTrendsException;

    abstract public void remove(ParserResult parserResult) throws QualityTrendsException;
}
