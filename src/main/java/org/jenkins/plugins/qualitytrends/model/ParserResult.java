package org.jenkins.plugins.qualitytrends.model;

import com.google.common.base.Objects;

/**
 * @author Emanuele Zattin
 */
public class ParserResult {
    private String parser;
    private Severity severity;
    private String file;
    private int lineNumber;
    private String issueId;
    private String message;
    private String link;

    public ParserResult(String parser, Severity severity, String file, int lineNumber, String message) {
        this.parser = parser;
        this.severity = severity;
        this.file = file;
        this.lineNumber = lineNumber;
        this.issueId = null;
        this.message = message;
        this.link = null;
    }

    public ParserResult(String parser, Severity severity, String file, int lineNumber, String issueId, String message, String link) {
        this.parser = parser;
        this.severity = severity;
        this.file = file;
        this.lineNumber = lineNumber;
        this.issueId = issueId;
        this.message = message;
        this.link = link;
    }

    public String getParser() {
        return parser;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getFile() {
        return file;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public String getIssueId() {
        return issueId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                getParser(),
                getSeverity(),
                getFile(),
                getLineNumber(),
                getMessage(),
                getLink()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParserResult) {
            ParserResult that = (ParserResult) o;
            return Objects.equal(this.getParser(), that.getParser())
                    && Objects.equal(this.getSeverity(), that.getSeverity())
                    && Objects.equal(this.getFile(), that.getFile())
                    && Objects.equal(this.getLineNumber(), that.getLineNumber())
                    && Objects.equal(this.getMessage(), that.getMessage())
                    && Objects.equal(this.getLink(), that.getLink());
        }
        return false;
    }
}
