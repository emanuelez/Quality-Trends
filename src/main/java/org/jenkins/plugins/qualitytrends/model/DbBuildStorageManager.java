package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import hudson.model.AbstractBuild;

import java.io.File;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public class DbBuildStorageManager implements BuildStorageManager {
    
    private int build_id;
    private DbController controller;

    @Inject
    public DbBuildStorageManager(@Assisted AbstractBuild build, DbControllerFactory controllerFactory) throws SQLException {
        String url = new File(build.getParent().getRootDir(), "qualityTrends").getAbsolutePath();
        this.controller = controllerFactory.create(url);
        build_id = controller.addBuildIfNew(build.getNumber());
    }

    public void addParserResult(ParserResult parserResult) throws QualityTrendsException {
        try {
            controller.addEntry(
                    build_id,
                    parserResult.getFile(),
                    parserResult.getLineNumber(),
                    parserResult.getParser(),
                    parserResult.getSeverity().toString(),
                    parserResult.getIssueId(),
                    parserResult.getMessage(),
                    parserResult.getLink());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QualityTrendsException("Could not add entry");
        }

    }

    public void remove(ParserResult parserResult) throws QualityTrendsException {
        // TODO: implement the method

    }

    public int getEntryNumberForBuild() throws QualityTrendsException {
        try {
            return controller.getEntryNumberFromBuild(build_id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QualityTrendsException("Could not count entries");
        }
    }

    public int getEntryNumberForBuildAndParser(Parser parser) throws QualityTrendsException {
        try {
            return controller.getEntryNumberFromBuildAndParser(build_id, parser.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QualityTrendsException("Could not count entries for parser");
        }
    }

    public Set<String> getFileNames() throws QualityTrendsException {
        try {
            return controller.getFileNames(build_id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QualityTrendsException("Could not get the file names");
        }
    }
}
