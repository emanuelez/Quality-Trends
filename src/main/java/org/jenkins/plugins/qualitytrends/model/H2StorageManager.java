package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import hudson.model.AbstractBuild;

import java.io.File;
import java.sql.SQLException;

/**
 * @author Emanuele Zattin
 */
public class H2StorageManager implements StorageManager {
    
    private int build_id;
    private DbController controller;

    @Inject
    public H2StorageManager(@Assisted AbstractBuild build) throws SQLException {
        String dbPath = new File(build.getParent().getRootDir(), "qualityTrends.h2").getAbsolutePath();
        System.out.println("Path: " + dbPath);
        controller = new H2Controller(dbPath);
        build_id = controller.addBuildIfNew(build.getNumber());
    }

    public void add(ParserResult parserResult) throws QualityTrendsException {
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
}
