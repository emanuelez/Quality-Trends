package org.jenkins.plugins.qualitytrends.model;

import hudson.model.AbstractBuild;

import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public interface StorageManager {

    abstract public void addParserResult(ParserResult parserResult) throws QualityTrendsException;

    abstract public void remove(ParserResult parserResult) throws QualityTrendsException;

    int getEntryNumberForBuild(AbstractBuild<?, ?> build) throws QualityTrendsException;
    
    int getEntryNumberForBuildAndParser(AbstractBuild<?, ?> build, Parser parser) throws QualityTrendsException;
}
