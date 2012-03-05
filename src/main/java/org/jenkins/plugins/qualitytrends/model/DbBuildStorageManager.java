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

    public void addParserResult(ParserResult parserResult) {
    controller.addEntry(
        buildNumber,
        parserResult.getFile(),
        parserResult.getLineNumber(),
        parserResult.getParser(),
        parserResult.getSeverity().toString(),
        parserResult.getIssueId(),
        parserResult.getMessage(),
        parserResult.getLink());
    }

    public int getEntryNumberForBuild(){
        return controller.getEntryNumberFromBuild(buildNumber);
    }

    public int getEntryNumberForBuildAndParser(Parser parser) {
        return controller.getEntryNumberFromBuildAndParser(buildNumber, parser.getName());
    }

    public Set<String> getFileNames() {
        return controller.getFileNames(buildNumber);
    }

    public void updateEntryWithFileSha1(String fileName, String fileSha1) {
        controller.associateFileSha1ToEntryForBuildAndFileName(buildNumber, fileName, fileSha1);
    }

    public Map<String, Integer> getNewFileSha1AndLineNumber() {
        Map<String, Integer> result;
            if (isFirstBuild()) {
                // get all the couples for this build
                result = controller.getFileSha1AndLineNumberForBuild(buildNumber);
            } else {
                // get only the new couples introduced by this build
                result = controller.getNewFileSha1AndLineNumberForBuild(buildNumber);
            }
        return result;
    }

    public Set<Entry> findEntriesForFileSha1AndLineNumber(String fileSha1, int lineNumber) {
        return controller.getEntriesForBuildFileSha1AndLineNumber(buildNumber, fileSha1, lineNumber);
    }

    public void addWarning(String warningSha1, Entry entry) {
        controller.associateWarningToEntry(warningSha1, entry.getEntryId());
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

    private boolean isFirstBuild() {
        return controller.countBuildsBefore(buildNumber) == 0;
    }
}
