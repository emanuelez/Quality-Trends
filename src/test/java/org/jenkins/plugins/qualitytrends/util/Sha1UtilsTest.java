package org.jenkins.plugins.qualitytrends.util;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class Sha1UtilsTest extends TestCase {
    public void testGenerateWarningSha1() throws Exception {


    }

    public void testGenerateContextSha1() throws URISyntaxException {
        File file = new File(Resources.getResource("Sha1Utils.txt").toURI());
        try {
            String sha1 = Sha1Utils.generateContextSha1(
                    Files.toString(file, Charset.defaultCharset()),
                    2);
            assertNotNull(sha1);
            assertTrue(sha1.length() == 40);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            fail();
        }

    }
}
