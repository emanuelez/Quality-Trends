package org.jenkins.plugins.qualitytrends.model;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */
public interface Parser extends Serializable {
    
    public String getName();
    
    public String getRegex();
    
    public ParserResult getParserResult(Matcher matcher);
    
}
