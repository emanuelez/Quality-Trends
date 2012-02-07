package org.jenkins.plugins.qualitytrends.model;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import hudson.model.AbstractBuild;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public class H2Handler implements ParserResultHandler {

    @Inject
    public H2Handler(@Assisted AbstractBuild build) {
        String dbPath = new File(build.getParent().getRootDir(), "qualityTrends.h2").getAbsolutePath();
        H2Controller controller = new H2Controller(dbPath);
    }

    public void add(ParserResult parserResult) throws QualityTrendsException {
        // TODO: implement the method

    }

    public void remove(ParserResult parserResult) throws QualityTrendsException {
        // TODO: implement the method

    }
}
