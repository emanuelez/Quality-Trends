package org.jenkins.plugins.qualitytrends.model;

public class EntryBuilder {
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

    public EntryBuilder setEntryId(int entryId) {
        this.entryId = entryId;
        return this;
    }

    public EntryBuilder setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
        return this;
    }

    public EntryBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public EntryBuilder setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public EntryBuilder setParser(String parser) {
        this.parser = parser;
        return this;
    }

    public EntryBuilder setSeverity(String severity) {
        this.severity = severity;
        return this;
    }

    public EntryBuilder setIssueId(String issueId) {
        this.issueId = issueId;
        return this;
    }

    public EntryBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public EntryBuilder setFileSha1(String fileSha1) {
        this.fileSha1 = fileSha1;
        return this;
    }

    public EntryBuilder setWarningSha1(String warningSha1) {
        this.warningSha1 = warningSha1;
        return this;
    }

    public Entry createEntry() {
        return new Entry(entryId, buildNumber, fileName, lineNumber, parser, severity, issueId, message, fileSha1, warningSha1);
    }
}