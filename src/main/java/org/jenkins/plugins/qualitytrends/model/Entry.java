package org.jenkins.plugins.qualitytrends.model;

/**
 * @author Emanuele Zattin
 */

public class Entry {
    private int entryId;
    private int buildNumber;
    private String fileName;
    private int lineNumber;
    private String parser;
    private String severity;
    private String issueId;
    private String message;
    private String fileSha1;
    private String warningSha1;

    public Entry(int entryId, int buildNumber, String fileName, int lineNumber, String parser, String severity, String issueId, String message, String fileSha1, String warningSha1) {
        this.entryId = entryId;
        this.buildNumber = buildNumber;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.parser = parser;
        this.severity = severity;
        this.issueId = issueId;
        this.message = message;
        this.fileSha1 = fileSha1;
        this.warningSha1 = warningSha1;
    }

    public int getEntryId() {
        return entryId;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getParser() {
        return parser;
    }

    public String getSeverity() {
        return severity;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getMessage() {
        return message;
    }

    public String getFileSha1() {
        return fileSha1;
    }

    public String getWarningSha1() {
        return warningSha1;
    }
}
