package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.internal.Nullable;

import java.sql.SQLException;
import java.util.Set;

/**
 * Defines the access to a database
 */
public interface DbController {
    /**
     * Adds a build to the DB
     * @param build_number the Jenkins build number
     * @return the build_id in the DB
     * @throws java.sql.SQLException in case of any problem
     */
    public int addBuild(int build_number) throws SQLException;

    /**
     * Associate a git commit SHA1 to the build
     * @param build_id the build id in the DB
     * @param commit_sha1 the SHA1 of the commit associated to the build
     * @throws java.sql.SQLException in case of any problem
     */
    public void associateCommitToBuild(int build_id, String commit_sha1) throws SQLException;

    /**
     * Add a parser result to the DB 
     * @param build_id the build id in the DB
     * @param file_name the file name. It can be absolute or relative
     * @param line_number the line number returned by the parser
     * @param parser the parser that produced this entry
     * @param severity the severity of the entry
     * @param issue_id the issue id in the specific tool being parsed. Can be null
     * @param message the message associated to the entry
     * @param link the link associated to the entry. Can be null
     * @return the entry id in the DB
     * @throws java.sql.SQLException in case of any problem
     */
    public int addEntry(
            int build_id, 
            String file_name, 
            int line_number, 
            String parser, 
            String severity,
            @Nullable String issue_id,
            String message,
            @Nullable String link) throws SQLException;

    /**
     * Associate a file SHA1 to an entry
     * @param entry_id the DB entry id to update
     * @param file_sha1 the SHA1 of the Git blob associated to the file
     * @throws java.sql.SQLException in case of any problem
     */
    public void associateFileSha1ToEntry(int entry_id, String file_sha1) throws SQLException;

    /**
     * Add a warning to the db if not existent
     * @param warning_sha1 the SHA1 of the warning
     * @return the warning id in the DB
     * @throws java.sql.SQLException in case of any problem
     */
    public int addWarning(String warning_sha1) throws SQLException;

    /**
     * Associate a warning to an entry
     * @param warning_id the DB id of the warning
     * @param entry_id the DB id of the entry
     * @throws java.sql.SQLException in case of any problem
     */
    public void associateWarningToEntry(int warning_id, int entry_id) throws SQLException;

    /**
     * Deletes the tables of the DB. Useful for unit tests
     * @throws SQLException in case of any problem
     */
    public void tearDownDb() throws SQLException;

    /**
     * Add a build if its number is not in the DB already
     * @param build_number Jenkins build number
     * @throws SQLException in case of any problem
     * @return the build id in the DB
     */
    public int addBuildIfNew(int build_number) throws SQLException;

    /**
     * Get a build from its build number
     * @param build_number Jenkins build number
     * @return the Build object relative to the build number or null if none was found
     * @throws SQLException in case of any problem
     */
    public Build getBuildFromBuildNumber(int build_number) throws SQLException;

    int getEntryNumberFromBuildNumber(int build_number) throws SQLException;

    int getEntryNumberFromBuildNumberAndParser(int build_number, String parser) throws SQLException;

    Set<String> getFileNames(int build_id) throws SQLException;
}
