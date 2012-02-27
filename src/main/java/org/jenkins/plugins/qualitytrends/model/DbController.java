package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.internal.Nullable;

import java.sql.SQLException;
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
     * @throws java.sql.SQLException in case of any problem
     */
    int addEntry(
            int buildNumber,
            String fileName,
            int lineNumber,
            String parser,
            String severity,
            @Nullable String issueId,
            String message,
            @Nullable String link) throws SQLException;

    /**
     * Associate a warning to an entry
     *
     * @param warningSha1 the warning SHA1
     * @param entryId     the DB id of the entry
     * @throws java.sql.SQLException in case of any problem
     */
    void associateWarningToEntry(String warningSha1, int entryId) throws SQLException;

    /**
     * Deletes the tables of the DB. Useful for unit tests
     *
     * @throws SQLException in case of any problem
     */
    void tearDownDb() throws SQLException;

    int getEntryNumberFromBuild(int buildNumber) throws SQLException;

    int getEntryNumberFromBuildAndParser(int buildNumber, String parser) throws SQLException;

    Set<String> getFileNames(int buildNumber) throws SQLException;

    void associateFileSha1ToEntryForBuildAndFileName(int buildNumber, String fileName, String fileSha1) throws SQLException;

    int countBuildsBefore(int buildNumber) throws SQLException;

    Map<String, Integer> getFileSha1AndLineNumberForBuild(int buildNumber) throws SQLException;

    Map<String, Integer> getNewFileSha1AndLineNumberForBuild(int buildNumber) throws SQLException;

    Set<Entry> getEntriesForBuildFileSha1AndLineNumber(int buildNumber, String fileSha1, int lineNumber) throws SQLException;

    int countInfosForBuild(int buildNumber);

    int countWarningsForBuild(int buildNumber);

    int countErrorsForBuild(int buildNumber);

    int countOrphansForBuild(int buildNumber);
}
