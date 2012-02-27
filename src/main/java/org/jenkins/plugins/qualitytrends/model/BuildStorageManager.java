package org.jenkins.plugins.qualitytrends.model;

import java.util.Map;
import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public interface BuildStorageManager {

    void addParserResult(ParserResult parserResult) throws QualityTrendsException;

    int getEntryNumberForBuild() throws QualityTrendsException;

    int getEntryNumberForBuildAndParser(Parser parser) throws QualityTrendsException;

    Set<String> getFileNames() throws QualityTrendsException;

    void updateEntryWithFileSha1(String fileName, String FileSha1) throws QualityTrendsException;

    Map<String, Integer> getNewFileSha1AndLineNumber() throws QualityTrendsException;

    Set<Entry> findEntriesForFileSha1AndLineNumber(String key, int value) throws QualityTrendsException;

    void addWarning(String warningSha1, Entry entry) throws QualityTrendsException;

    int getInfos();

    int getWarnings();

    int getErrors();

    int getOrphans();
}
