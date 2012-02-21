package org.jenkins.plugins.qualitytrends.parsers;

import hudson.Extension;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;
import org.jenkins.plugins.qualitytrends.model.Severity;

import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */

@Extension
public class Armcc extends Parser {
    @Override
    public String getName() {
        return "Armcc";
    }

    @Override
    public String getRegex() {
        return "^\"(.+)\", line (\\d+): ([A-Z][a-z]+):\\D*(\\d+)\\D*?:\\s+(.+)$";
    }

    @Override
    public ParserResult getParserResult(Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = Integer.parseInt(matcher.group(2));
        Severity severity = "error".equalsIgnoreCase(matcher.group(3))?Severity.ERROR:Severity.WARNING;
        String errorCode = matcher.group(4);
        String message = matcher.group(5);

        return new ParserResult(getName(), severity, fileName, lineNumber, errorCode, message, null);
    }
}
