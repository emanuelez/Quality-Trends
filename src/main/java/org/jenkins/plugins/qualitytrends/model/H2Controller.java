package org.jenkins.plugins.qualitytrends.model;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
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
    private PreparedStatement countSeverityForBuild;
    private PreparedStatement countOrphansForBuild;

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

        url = Resources.getResource(this.getClass(), "countSeverityForBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        countSeverityForBuild = connection.prepareStatement(sql);

        url = Resources.getResource(this.getClass(), "countOrphansForBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        countOrphansForBuild = connection.prepareStatement(sql);
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
            @Nullable String link){
        try {
            addEntry.setInt(1, buildId);
            addEntry.setClob(2, new StringReader(fileName));
            addEntry.setInt(3, lineNumber);
            addEntry.setString(4, parser);
            addEntry.setString(5, severity);
            addEntry.setString(6, issueId);
            addEntry.setClob(7, new StringReader(message));
            addEntry.setClob(8, link != null?new StringReader(link):null);
            addEntry.executeUpdate();
            ResultSet generatedKeys = addEntry.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }
    }

    public void associateFileSha1ToEntryForBuildAndFileName(int buildNumber, String fileName, String fileSha1) {
        try {
            associateFileSha1ToEntryForBuildAndFileName.setString(1, fileSha1);
            associateFileSha1ToEntryForBuildAndFileName.setInt(2, buildNumber);
            associateFileSha1ToEntryForBuildAndFileName.setClob(3, new StringReader(fileName));
            associateFileSha1ToEntryForBuildAndFileName.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }

    public int countBuildsBefore(int buildNumber) {
        try {
            countBuildsBefore.setInt(1, buildNumber);
            countBuildsBefore.executeQuery();
            ResultSet resultSet = countBuildsBefore.getResultSet();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }

    }

    public Map<String, Integer> getFileSha1AndLineNumberForBuild(int buildNumber) {
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        }

    }

    public Map<String, Integer> getNewFileSha1AndLineNumberForBuild(int buildNumber) {
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        }

    }

    public Set<Entry> getEntriesForBuildFileSha1AndLineNumber(int buildNumber, String fileSha1, int lineNumber) {
        try {
            getEntriesForBuildFileSha1AndLineNumber.setInt(1, buildNumber);
            getEntriesForBuildFileSha1AndLineNumber.setString(2, fileSha1);
            getEntriesForBuildFileSha1AndLineNumber.setInt(3, lineNumber);
            ResultSet resultSet = getEntriesForBuildFileSha1AndLineNumber.executeQuery();
            Set<Entry> result = Sets.newHashSet();
            while (resultSet.next()) {
                Reader in = resultSet.getClob("file_name").getCharacterStream();
                StringWriter out = new StringWriter();
                CharStreams.copy(in, out);
                String fileName = out.toString();

                in = resultSet.getClob("message").getCharacterStream();
                out = new StringWriter();
                CharStreams.copy(in, out);
                String message = out.toString();

                in = resultSet.getClob("link").getCharacterStream();
                out = new StringWriter();
                CharStreams.copy(in, out);
                String link = out.toString();

                Entry entry = new EntryBuilder()
                        .setBuildNumber(resultSet.getInt("build_number"))
                        .setEntryId(resultSet.getInt("build_id"))
                        .setFileName(fileName)
                        .setFileSha1(resultSet.getString("file_sha1"))
                        .setIssueId(resultSet.getString("issue_id"))
                        .setLineNumber(resultSet.getInt("line_number"))
                        .setMessage(message)
                        .setLink(link)
                        .setParser(resultSet.getString("parser"))
                        .setSeverity(resultSet.getString("severity"))
                        .setWarningSha1(resultSet.getString("warning_sha1"))
                        .createEntry();
                result.add(entry);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        }

    }

    public int countInfosForBuild(int buildNumber) {
        try {
            countSeverityForBuild.setInt(1, buildNumber);
            countSeverityForBuild.setString(2, Severity.INFO.toString());
            ResultSet resultSet = countSeverityForBuild.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }
    }

    public int countWarningsForBuild(int buildNumber) {
        try {
            countSeverityForBuild.setInt(1, buildNumber);
            countSeverityForBuild.setString(2, Severity.WARNING.toString());
            ResultSet resultSet = countSeverityForBuild.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }
    }

    public int countErrorsForBuild(int buildNumber) {
        try {
            countSeverityForBuild.setInt(1, buildNumber);
            countSeverityForBuild.setString(2, Severity.ERROR.toString());
            ResultSet resultSet = countSeverityForBuild.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }
    }

    public int countOrphansForBuild(int buildNumber) {
        try {
            countOrphansForBuild.setInt(1, buildNumber);
            ResultSet resultSet = countOrphansForBuild.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }
    }

    public void associateWarningToEntry(String warningSha1, int entryId) {
        try {
            associateWarningToEntry.setString(1, warningSha1);
            associateWarningToEntry.setInt(2, entryId);
            associateWarningToEntry.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }

    }

    public void tearDownDb() {
        try {
            tearDown.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }

    public int getEntryNumberFromBuild(int buildNumber) {
        try {
            getEntryNumberFromBuild.setInt(1, buildNumber);
            getEntryNumberFromBuild.execute();
            ResultSet resultSet = getEntryNumberFromBuild.getResultSet();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }

    }

    public int getEntryNumberFromBuildAndParser(int buildNumber, String parser) {
        try {
            getEntryNumberFromBuildAndParser.setInt(1, buildNumber);
            getEntryNumberFromBuildAndParser.setString(2, parser);
            getEntryNumberFromBuildAndParser.execute();
            ResultSet resultSet = getEntryNumberFromBuildAndParser.getResultSet();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return 0;
        }

    }

    public Set<String> getFileNames(int build_id) {
        try {
            getFileNamesFromBuild.setInt(1, build_id);
            getFileNamesFromBuild.execute();
            ResultSet resultSet = getFileNamesFromBuild.getResultSet();
            Set<String> fileNames = Sets.newHashSet();
            while (resultSet.next()) {
                Reader in = resultSet.getClob(1).getCharacterStream();
                StringWriter out = new StringWriter();
                CharStreams.copy(in, out);
                fileNames.add(out.toString());
            }
            return fileNames;
        } catch (SQLException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.propagate(e);
            return null;
        }

    }
}
