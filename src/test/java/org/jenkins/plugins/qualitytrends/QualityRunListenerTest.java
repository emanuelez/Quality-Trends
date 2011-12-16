package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.listeners.RunListener;
import hudson.tasks.Shell;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;
import org.jenkins.plugins.qualitytrends.model.Severity;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.util.regex.Matcher;

import static org.mockito.Mockito.*;

/**
 * @author Emanuele Zattin
 */
public class QualityRunListenerTest extends HudsonTestCase {

    public void testListener() throws Exception {

        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

        for (String line : Files.readLines(build.getLogFile(), Charsets.ISO_8859_1)) {
            System.out.println(line);
        }

        QualityRunListener runListener = RunListener.all().get(QualityRunListener.class);


        System.out.println("Parser results: " + runListener.getParserResults().size());
        

    }
    
    private Parser createMockParser() {
        Parser parser = mock(Parser.class);
        
        when(parser.getRegex()).thenReturn("^*$");
        when(parser.getParserResult(any(Matcher.class))).thenReturn(new ParserResult(
                "Bogus Parser",
                Severity.WARNING,
                new File("/some/random/path"),
                42,
                "Random message"));

        return parser;
    }
}
