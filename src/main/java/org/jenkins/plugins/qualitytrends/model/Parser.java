package org.jenkins.plugins.qualitytrends.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Hudson;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */
public abstract class Parser implements ExtensionPoint, Serializable {
    
    public abstract String getName();
    
    public abstract String getRegex();
    
    public abstract ParserResult getParserResult(Matcher matcher);
    
    public static ExtensionList<Parser> all() {
        return Hudson.getInstance().getExtensionList(Parser.class);
    }
    
}
