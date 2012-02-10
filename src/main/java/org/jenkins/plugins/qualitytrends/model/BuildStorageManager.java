package org.jenkins.plugins.qualitytrends.model;

import hudson.model.AbstractBuild;

import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public interface BuildStorageManager {

    abstract public void addParserResult(ParserResult parserResult) throws QualityTrendsException;

    abstract public void remove(ParserResult parserResult) throws QualityTrendsException;

    int getEntryNumberForBuild() throws QualityTrendsException;
    
    int getEntryNumberForBuildAndParser(Parser parser) throws QualityTrendsException;

    Set<String> getFileNames() throws QualityTrendsException;
}
