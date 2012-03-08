package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.internal.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Defines the access to a database
 */
public interface DbController {
    /**
     * Add a parser result to the DB
     *
     * @param buildNumber the build number
     * @param fileName    the file name. It can be absolute or relative
     * @param lineNumber  the line number returned by the parser
     * @param parser      the parser that produced this entry
     * @param severity    the severity of the entry
     * @param issueId     the issue id in the specific tool being parsed. Can be null
     * @param message     the message associated to the entry
     * @param link        the link associated to the entry. Can be null
     * @return the entry id in the DB
     */
    int addEntry(
            int buildNumber,
            String fileName,
            int lineNumber,
            String parser,
            String severity,
            @Nullable String issueId,
            String message,
            @Nullable String link);

    /**
     * Associate a warning to an entry
     *
     * @param warningSha1 the warning SHA1
     * @param entryId     the DB id of the entry
     */
    void associateWarningToEntry(String warningSha1, int entryId);

    /**
     * Deletes the tables of the DB. Useful for unit tests
     *
     */
    void tearDownDb();

    int getEntryNumberFromBuild(int buildNumber);

    int getEntryNumberFromBuildAndParser(int buildNumber, String parser);

    Set<String> getFileNames(int buildNumber);

    void associateFileSha1ToEntryForBuildAndFileName(int buildNumber, String fileName, String fileSha1);

    int countBuildsBefore(int buildNumber);

    Map<String, Integer> getFileSha1AndLineNumberForBuild(int buildNumber);

    Map<String, Integer> getNewFileSha1AndLineNumberForBuild(int buildNumber);

    Set<Entry> getEntriesForBuildFileSha1AndLineNumber(int buildNumber, String fileSha1, int lineNumber);

    int countOrphansForBuild(int buildNumber);

    Map<String, Integer> getSeverities(int buildNumber);

    Map<String, Integer> getParsers(int buildNumber);
}
