package org.jenkins.plugins.qualitytrends.model;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Nullable;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Map;
import java.util.Set;

public class H2Controller implements DbController {

    private Connection connection;
    private PreparedStatement addEntry;
    private PreparedStatement associateFileSha1ToEntryForBuildAndFileName;
    private PreparedStatement associateWarningToEntry;
    private PreparedStatement tearDown;
    private PreparedStatement getEntriesForBuildFileSha1AndLineNumber;
    private PreparedStatement getEntryNumberFromBuild;
    private PreparedStatement getEntryNumberFromBuildAndParser;
    private PreparedStatement getFileNamesFromBuild;
    private PreparedStatement countBuildsBefore;
    private PreparedStatement getFileSha1AndLineNumberForBuild;
    private PreparedStatement getNewFileSha1AndLineNumberForBuild;

    @Inject
    public H2Controller(@Assisted String path) {
        try {
            Class.forName("org.h2.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection("jdbc:h2:" + path, "sa", "");

            if (!isSchemaOK()) {
                createSchema();
            }

            setupPreparedStatements();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not initialize the DB");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("An IO exception occurred while initializing the DB");
        }
    }

    private void setupPreparedStatements() throws IOException, SQLException {
        URL url = Resources.getResource(this.getClass(), "addEntry.sql");
        String sql = Resources.toString(url, Charsets.ISO_8859_1);
        addEntry = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        url = Resources.getResource(this.getClass(), "associateFileSha1ToEntryForBuildAndFileName.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        associateFileSha1ToEntryForBuildAndFileName = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "associateWarningToEntry.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        associateWarningToEntry = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "tearDown.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        tearDown = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getEntryNumberFromBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getEntryNumberFromBuild = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getEntryNumberFromBuildAndParser.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getEntryNumberFromBuildAndParser = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getFileNamesFromBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getFileNamesFromBuild = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "countBuildsBefore.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        countBuildsBefore = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getFileSha1AndLineNumberForBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getFileSha1AndLineNumberForBuild = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getNewFileSha1AndLineNumberForBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getNewFileSha1AndLineNumberForBuild = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "getEntriesForBuildFileSha1AndLineNumber.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getEntriesForBuildFileSha1AndLineNumber = connection.prepareStatement(sql);
    }

    private boolean isSchemaOK() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", null);
        boolean isOk = false;
        while (tables.next()) {
            String table_name = tables.getString("TABLE_NAME");
            if ("ENTRIES".equals(table_name)) {
                isOk = true;
            }
        }
        return isOk;
    }

    private void createSchema() throws IOException, SQLException {
        URL url = Resources.getResource(this.getClass(), "schema.sql");
        String sql = Resources.toString(url, Charsets.ISO_8859_1);
        PreparedStatement createSchema = connection.prepareStatement(sql);
        createSchema.execute();
    }

    public int addEntry(
            int buildId,
            String fileName,
            int lineNumber,
            String parser,
            String severity,
            @Nullable String issueId,
            String message,
            @Nullable String link) throws SQLException {
        addEntry.setInt(1, buildId);
        addEntry.setString(2, fileName);
        addEntry.setInt(3, lineNumber);
        addEntry.setString(4, parser);
        addEntry.setString(5, severity);
        addEntry.setString(6, issueId);
        addEntry.setString(7, message);
        addEntry.setString(8, link);
        addEntry.executeUpdate();
        ResultSet generatedKeys = addEntry.getGeneratedKeys();
        generatedKeys.next();
        return generatedKeys.getInt(1);
    }

    public void associateFileSha1ToEntryForBuildAndFileName(int buildNumber, String fileName, String fileSha1) throws SQLException {
        associateFileSha1ToEntryForBuildAndFileName.setString(1, fileSha1);
        associateFileSha1ToEntryForBuildAndFileName.setInt(2, buildNumber);
        associateFileSha1ToEntryForBuildAndFileName.setString(3, fileName);
        associateFileSha1ToEntryForBuildAndFileName.executeUpdate();
    }

    public int countBuildsBefore(int buildNumber) throws SQLException {
        countBuildsBefore.setInt(1, buildNumber);
        countBuildsBefore.executeQuery();
        ResultSet resultSet = countBuildsBefore.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public Map<String, Integer> getFileSha1AndLineNumberForBuild(int buildNumber) throws SQLException {
        getFileSha1AndLineNumberForBuild.setInt(1, buildNumber);
        getFileSha1AndLineNumberForBuild.executeQuery();
        ResultSet resultSet = getFileSha1AndLineNumberForBuild.getResultSet();
        Map<String, Integer> result = Maps.newHashMap();
        while (resultSet.next()) {
            result.put(
                    resultSet.getString("file_sha1"),
                    resultSet.getInt("line_number"));
        }
        return result;
    }

    public Map<String, Integer> getNewFileSha1AndLineNumberForBuild(int buildNumber) throws SQLException {
        getNewFileSha1AndLineNumberForBuild.setInt(1, buildNumber);
        getNewFileSha1AndLineNumberForBuild.setInt(2, buildNumber);
        getNewFileSha1AndLineNumberForBuild.executeQuery();
        ResultSet resultSet = getNewFileSha1AndLineNumberForBuild.getResultSet();
        Map<String, Integer> result = Maps.newHashMap();
        while (resultSet.next()) {
            result.put(
                    resultSet.getString("file_sha1"),
                    resultSet.getInt("line_number"));
        }
        return result;
    }

    public Set<Entry> getEntriesForBuildFileSha1AndLineNumber(int buildNumber, String fileSha1, int lineNumber) throws SQLException {
        getEntriesForBuildFileSha1AndLineNumber.setInt(1, buildNumber);
        getEntriesForBuildFileSha1AndLineNumber.setString(2, fileSha1);
        getEntriesForBuildFileSha1AndLineNumber.setInt(3, lineNumber);
        ResultSet resultSet = getEntriesForBuildFileSha1AndLineNumber.executeQuery();
        Set<Entry> result = Sets.newHashSet();
        while (resultSet.next()) {
            Entry entry = new EntryBuilder()
                    .setBuildNumber(resultSet.getInt("build_number"))
                    .setEntryId(resultSet.getInt("build_id"))
                    .setFileName(resultSet.getString("file_name"))
                    .setFileSha1(resultSet.getString("file_sha1"))
                    .setIssueId(resultSet.getString("issue_id"))
                    .setLineNumber(resultSet.getInt("line_number"))
                    .setMessage(resultSet.getString("message"))
                    .setParser(resultSet.getString("parser"))
                    .setSeverity(resultSet.getString("severity"))
                    .setWarningSha1(resultSet.getString("warning_sha1"))
                    .createEntry();
            result.add(entry);
        }
        return result;
    }

    public void associateWarningToEntry(String warningSha1, int entryId) throws SQLException {
        associateWarningToEntry.setString(1, warningSha1);
        associateWarningToEntry.setInt(2, entryId);
        associateWarningToEntry.executeUpdate();
    }

    public void tearDownDb() throws SQLException {
        tearDown.execute();
    }

    public int getEntryNumberFromBuild(int buildNumber) throws SQLException {
        getEntryNumberFromBuild.setInt(1, buildNumber);
        getEntryNumberFromBuild.execute();
        ResultSet resultSet = getEntryNumberFromBuild.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public int getEntryNumberFromBuildAndParser(int buildNumber, String parser) throws SQLException {
        getEntryNumberFromBuildAndParser.setInt(1, buildNumber);
        getEntryNumberFromBuildAndParser.setString(2, parser);
        getEntryNumberFromBuildAndParser.execute();
        ResultSet resultSet = getEntryNumberFromBuildAndParser.getResultSet();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public Set<String> getFileNames(int build_id) throws SQLException {
        getFileNamesFromBuild.setInt(1, build_id);
        getFileNamesFromBuild.execute();
        ResultSet resultSet = getFileNamesFromBuild.getResultSet();
        Set<String> fileNames = Sets.newHashSet();
        while (resultSet.next()) {
            fileNames.add(resultSet.getString(1));
        }
        return fileNames;
    }
}
