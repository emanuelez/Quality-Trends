package org.jenkins.plugins.qualitytrends.model;

import hudson.model.Run;

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

    int getInfos();

    int getWarnings();

    int getErrors();

    int getOrphans();

    int getInfos(Run previousSuccessfulBuild);

    int getWarnings(Run previousSuccessfulBuild);

    int getErrors(Run previousSuccessfulBuild);

    int getOrphans(Run previousSuccessfulBuild);
}
