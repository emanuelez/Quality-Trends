package org.jenkins.plugins.qualitytrends.model;

import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */
public interface Parser {
    
    public String getName();
    
    public String getRegex();
    
    public ParserResult getParserResult(Matcher matcher);
    
}
