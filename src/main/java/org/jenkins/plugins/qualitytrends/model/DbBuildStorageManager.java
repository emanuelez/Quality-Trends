package org.jenkins.plugins.qualitytrends.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import hudson.model.AbstractBuild;
import hudson.model.Run;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * @author Emanuele Zattin
 */
public class DbBuildStorageManager implements BuildStorageManager {

    private int buildNumber;
    private DbController controller;

    @Inject
    public DbBuildStorageManager(@Assisted AbstractBuild build, DbControllerFactory controllerFactory) throws SQLException {
        String url = new File(build.getParent().getRootDir(), "qualityTrends").getAbsolutePath();
        this.controller = controllerFactory.create(url);
        buildNumber = build.getNumber();
    }

    public void addParserResult(ParserResult parserResult) throws QualityTrendsException {
        try {
            controller.addEntry(
                    buildNumber,
                    parserResult.getFile(),
                    parserResult.getLineNumber(),
                    parserResult.getParser(),
                    parserResult.getSeverity().toString(),
                    parserResult.getIssueId(),
                    parserResult.getMessage(),
                    parserResult.getLink());
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not add entry");
        }

    }

    public int getEntryNumberForBuild() throws QualityTrendsException {
        try {
            return controller.getEntryNumberFromBuild(buildNumber);
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not count entries");
        }
    }

    public int getEntryNumberForBuildAndParser(Parser parser) throws QualityTrendsException {
        try {
            return controller.getEntryNumberFromBuildAndParser(buildNumber, parser.getName());
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not count entries for parser");
        }
    }

    public Set<String> getFileNames() throws QualityTrendsException {
        try {
            return controller.getFileNames(buildNumber);
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not get the file names");
        }
    }

    public void updateEntryWithFileSha1(String fileName, String fileSha1) throws QualityTrendsException {
        try {
            controller.associateFileSha1ToEntryForBuildAndFileName(buildNumber, fileName, fileSha1);
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not update the entries");
        }
    }

    public Map<String, Integer> getNewFileSha1AndLineNumber() throws QualityTrendsException {
        Map<String, Integer> result;
        try {
            if (isFirstBuild()) {
                // get all the couples for this build
                result = controller.getFileSha1AndLineNumberForBuild(buildNumber);
            } else {
                // get only the new couples introduced by this build
                result = controller.getNewFileSha1AndLineNumberForBuild(buildNumber);
            }
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not get the previous build");
        }
        return result;
    }

    public Set<Entry> findEntriesForFileSha1AndLineNumber(String fileSha1, int lineNumber) throws QualityTrendsException {
        Set<Entry> result;
        try {
            result = controller.getEntriesForBuildFileSha1AndLineNumber(buildNumber, fileSha1, lineNumber);
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not get the entries for the specified file SHA1 and line number");
        }
        return result;
    }

    public void addWarning(String warningSha1, Entry entry) throws QualityTrendsException {
        try {
            controller.associateWarningToEntry(warningSha1, entry.getEntryId());
        } catch (SQLException e) {
            throw new QualityTrendsException("Could not associate warning to entry");
        }
    }

    public int getInfos() {
        return controller.countInfosForBuild(buildNumber);
    }

    public int getWarnings() {
        return controller.countWarningsForBuild(buildNumber);
    }

    public int getErrors() {
        return controller.countErrorsForBuild(buildNumber);
    }

    public int getOrphans() {
        return controller.countOrphansForBuild(buildNumber);
    }

    public int getInfos(Run previousSuccessfulBuild) {
        return controller.countInfosForBuild(previousSuccessfulBuild.getNumber());
    }

    public int getWarnings(Run previousSuccessfulBuild) {
        return controller.countWarningsForBuild(previousSuccessfulBuild.getNumber());
    }

    public int getErrors(Run previousSuccessfulBuild) {
        return controller.countErrorsForBuild(previousSuccessfulBuild.getNumber());
    }

    public int getOrphans(Run previousSuccessfulBuild) {
        return controller.countOrphansForBuild(previousSuccessfulBuild.getNumber());
    }

    private boolean isFirstBuild() throws SQLException {
        return controller.countBuildsBefore(buildNumber) == 0;
    }
}
