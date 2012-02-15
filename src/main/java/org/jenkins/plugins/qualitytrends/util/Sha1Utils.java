package org.jenkins.plugins.qualitytrends.util;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author Emanuele Zattin
 */
public class Sha1Utils {
    
    public static String generateSha1(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(s.getBytes());
        return Hex.encodeHexString(md.digest());
    }
    
    public static String generateWarningSha1(String contextSha1, String parser, String severity, String message) throws NoSuchAlgorithmException {
        Preconditions.checkNotNull(contextSha1);
        Preconditions.checkNotNull(parser);
        Preconditions.checkNotNull(severity);
        Preconditions.checkNotNull(message);

        List<String> toDigest = Lists.newArrayList();
        toDigest.add(contextSha1);
        toDigest.add(parser);
        toDigest.add(severity);
        toDigest.add(message);
        return generateSha1(Joiner.on("|").join(toDigest));
    }
    
    public static String generateContextSha1(String file, int lineNumber) throws IOException, NoSuchAlgorithmException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(lineNumber > 0);

        BufferedReader reader = new BufferedReader(new StringReader(file));
        int startLine = Math.max(lineNumber - 3, 1);
        int endLine = lineNumber + 3;
        List<String> toDigest = Lists.newArrayList();

        // We don't really care about these lines
        for(int i = 0; i < startLine; i++) {
            reader.readLine();
        }

        for(int i = startLine; i <= endLine; i++) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            toDigest.add(line);
        }

        return generateSha1(Joiner.on("|").join(toDigest));
    }
}
