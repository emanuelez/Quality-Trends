package org.jenkins.plugins.qualitytrends.model;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Nullable;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Set;

public class H2Controller implements DbController {

    private Connection connection;
    private PreparedStatement addBuild;
    private PreparedStatement associateCommitToBuild;
    private PreparedStatement addEntry;
    private PreparedStatement associateFileSha1ToEntry;
    private PreparedStatement addWarning;
    private PreparedStatement associateWarningToEntry;
    private PreparedStatement tearDown;
    private PreparedStatement getBuildFromBuildNumber;
    private PreparedStatement getEntryNumberFromBuild;
    private PreparedStatement getEntryNumberFromBuildAndParser;
    private PreparedStatement getFileNamesFromBuild;

    @Inject
    public H2Controller(@Assisted String path) {
        try {
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupPreparedStatements() throws IOException, SQLException {

        URL url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/addBuild.sql");
        String sql = Resources.toString(url, Charsets.ISO_8859_1);
        addBuild = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/associateCommitToBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        associateCommitToBuild = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/addEntry.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        addEntry = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/associateFileSha1ToEntry.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        associateFileSha1ToEntry = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/addWarning.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        addWarning = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/associateWarningToEntry.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        associateWarningToEntry = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/tearDown.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        tearDown = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/getBuildFromBuildNumber.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getBuildFromBuildNumber = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/getEntryNumberFromBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getEntryNumberFromBuild = connection.prepareStatement(sql);

        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/getEntryNumberFromBuildAndParser.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getEntryNumberFromBuildAndParser = connection.prepareStatement(sql);
        
        url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/getFileNamesFromBuild.sql");
        sql = Resources.toString(url, Charsets.ISO_8859_1);
        getFileNamesFromBuild = connection.prepareStatement(sql);
    }

    private boolean isSchemaOK() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", null);
        int tablesToCheck = 3;
        while (tables.next()) {
            String table_name = tables.getString("TABLE_NAME");
            if ("BUILDS".equals(table_name)
                    ||"ENTRIES".equals(table_name)
                    ||"WARNINGS".equals(table_name)) {
                tablesToCheck--;
            }
        }
        return tablesToCheck==0;
    }

    private void createSchema() throws IOException, SQLException {
        URL url = Resources.getResource("org/jenkins/plugins/qualityTrends/sql/schema.sql");
        String sql = Resources.toString(url, Charsets.ISO_8859_1);
        PreparedStatement createSchema = connection.prepareStatement(sql);
        createSchema.execute();
    }

    public int addBuild(int build_number) throws SQLException {
        addBuild.setInt(1, build_number);
        addBuild.executeUpdate();
        ResultSet generatedKeys = addBuild.getGeneratedKeys();
        generatedKeys.next();
        return generatedKeys.getInt(1);
    }

    public void associateCommitToBuild(int build_id, String commit_sha1) throws SQLException {
        associateCommitToBuild.setString(1, commit_sha1);
        associateCommitToBuild.setInt(2, build_id);
        associateCommitToBuild.executeUpdate();
    }

    public int addEntry(
            int build_id,
            String file_name,
            int line_number,
            String parser,
            String severity,
            @Nullable String issue_id,
            String message,
            @Nullable String link) throws SQLException {
        addEntry.setInt(1, build_id);
        addEntry.setString(2, file_name);
        addEntry.setInt(3, line_number);
        addEntry.setString(4, parser);
        addEntry.setString(5, severity);
        addEntry.setString(6, issue_id);
        addEntry.setString(7, message);
        addEntry.setString(8, link);
        addEntry.executeUpdate();
        ResultSet generatedKeys = addEntry.getGeneratedKeys();
        generatedKeys.next();
        return generatedKeys.getInt(1);
    }

    public void associateFileSha1ToEntry(int entry_id, String file_sha1) throws SQLException {
        associateFileSha1ToEntry.setString(1, file_sha1);
        associateFileSha1ToEntry.setInt(2, entry_id);
        associateFileSha1ToEntry.executeUpdate();
    }

    public int addWarning(String warning_sha1) throws SQLException {
        addWarning.setString(1, warning_sha1);
        addWarning.executeUpdate();
        ResultSet generatedKeys = addWarning.getGeneratedKeys();
        generatedKeys.next();
        return generatedKeys.getInt(1);
    }

    public void associateWarningToEntry(int warning_id, int entry_id) throws SQLException {
        associateWarningToEntry.setInt(1, warning_id);
        associateWarningToEntry.setInt(2, entry_id);
        associateWarningToEntry.executeUpdate();
    }

    public void tearDownDb() throws SQLException {
        tearDown.execute();
    }

    public Build getBuildFromBuildNumber(int build_number) throws SQLException {
        getBuildFromBuildNumber.setInt(1, build_number);
        getBuildFromBuildNumber.execute();
        ResultSet resultSet = getBuildFromBuildNumber.getResultSet();
        if (resultSet.next()) {
            Build build = new Build(
                    resultSet.getInt("build_id"), 
                    resultSet.getInt("build_number"),
                    resultSet.getString("commit_sha1"));
            return build;
        } else {
            return null;
        }
    }

    public int getEntryNumberFromBuild(int build_id) throws SQLException {
        getEntryNumberFromBuild.setInt(1, build_id);
        getEntryNumberFromBuild.execute();
        ResultSet resultSet = getEntryNumberFromBuild.getResultSet();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            return 0;
        }
    }

    public int getEntryNumberFromBuildAndParser(int build_id, String parser) throws SQLException {
        getEntryNumberFromBuildAndParser.setInt(1, build_id);
        getEntryNumberFromBuildAndParser.setString(2, parser);
        getEntryNumberFromBuildAndParser.execute();
        ResultSet resultSet = getEntryNumberFromBuildAndParser.getResultSet();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            return 0;
        }
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

    public int addBuildIfNew(int build_number) throws SQLException {
        Build build = getBuildFromBuildNumber(build_number);
        if (build == null) {
            return addBuild(build_number);
        } else {
            return build.getBuild_id();
        }
    }
}
