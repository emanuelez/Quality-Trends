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
public class GccCompiler extends Parser {
    @Override
    public String getName() {
        return "GCC Compiler";
    }

    @Override
    public String getRegex() {
        return "^(.+?):(\\\\d+):(?:\\\\d+:)? (warning|error): (.*)$";
    }

    @Override
    public ParserResult getParserResult(Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = Integer.parseInt(matcher.group(2));
        Severity severity = "error".equalsIgnoreCase(matcher.group(3))?Severity.ERROR:Severity.WARNING;
        String message = matcher.group(4);

        return new ParserResult(getName(), severity, fileName, lineNumber, message);
    }
}
