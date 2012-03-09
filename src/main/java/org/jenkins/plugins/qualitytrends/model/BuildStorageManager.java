package org.jenkins.plugins.qualitytrends.model;

import java.util.Map;
import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public interface BuildStorageManager {

    void addParserResult(ParserResult parserResult);

    int getEntryNumberForBuild();

    int getEntryNumberForBuildAndParser(Parser parser);

    Set<String> getFileNames();

    void updateEntryWithFileSha1(String fileName, String FileSha1);

    Map<String, Integer> getNewFileSha1AndLineNumber();

    Set<Entry> findEntriesForFileSha1AndLineNumber(String key, int value);

    void addWarning(String warningSha1, Entry entry);

    Map<String, Integer> getOrphans();

    Map<String, Integer> getPreviousOrphans();

    Map<String, Integer> getSeverities();

    Map<String, Integer> getPreviousSeverities();

    Map<String, Integer> getParsers();

    Map<String, Integer> getPreviousParsers();
}
