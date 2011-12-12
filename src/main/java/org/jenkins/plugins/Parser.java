package org.jenkins.plugins;

import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */
public interface Parser {
    
    abstract public String getRegex();
    
    abstract public ParserResult getParserResult(Matcher matcher);
    
}
