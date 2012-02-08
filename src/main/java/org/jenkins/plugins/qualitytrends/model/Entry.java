package org.jenkins.plugins.qualitytrends.model;

/**
 * @author Emanuele Zattin
 */

public class Entry {
    private int entry_id;
    private int build_id;
    private String file_name;
    private int line_number;
    private String parser;
    private String severity;
    private String issue_id;
    private String message;
    private String file_sha1;
    private String warning_id;

    public Entry(int entry_id, int build_id, String file_name, int line_number, String parser, String severity, String issue_id, String message, String file_sha1, String warning_id) {
        this.entry_id = entry_id;
        this.build_id = build_id;
        this.file_name = file_name;
        this.line_number = line_number;
        this.parser = parser;
        this.severity = severity;
        this.issue_id = issue_id;
        this.message = message;
        this.file_sha1 = file_sha1;
        this.warning_id = warning_id;
    }

    public int getEntry_id() {
        return entry_id;
    }

    public int getBuild_id() {
        return build_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public int getLine_number() {
        return line_number;
    }

    public String getParser() {
        return parser;
    }

    public String getSeverity() {
        return severity;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public String getMessage() {
        return message;
    }

    public String getFile_sha1() {
        return file_sha1;
    }

    public String getWarning_id() {
        return warning_id;
    }
}
