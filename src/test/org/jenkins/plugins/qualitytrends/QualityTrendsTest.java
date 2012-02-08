package org.jenkins.plugins.qualitytrends;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;
import org.jenkins.plugins.qualitytrends.model.Severity;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Emanuele Zattin
 */
public class QualityTrendsTest extends HudsonTestCase {

    public void test(){
        FreeStyleProject project = null;
        try {
            project = createFreeStyleProject();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not create project");
        }
        try {
            project.getBuildersList().add(new Shell("echo hello"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not add Builder");
        }
        try {
            project.getPublishersList().add(new QualityTrends(createParserIterable()));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not add Publisher");
        }

        FreeStyleBuild build = null;
        try {
            build = project.scheduleBuild2(0).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("The build got interrupted");
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("There was an execution exception");
        }
        System.out.println(build.getDisplayName()+" completed");
        try {
            final List<String> logLines = Files.readLines(build.getLogFile(), Charset.defaultCharset());
            for (String logLine : logLines) {
                System.out.println(logLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read log file");
        }
    }
    
    private Parser createMockParser() {
        Parser parser = mock(Parser.class);
        when(parser.getRegex())
                .thenReturn("^.*$");
        when(parser.getParserResult(any(Matcher.class)))
                .thenReturn(new ParserResult(
                        "Mock Parser",
                        Severity.INFO,
                        ".",
                        0,
                        "Mock message" ));
        return parser;
    }
    
    private Iterable<Parser> createParserIterable() {
        Set<Parser> i = Sets.newHashSet();
        i.add(createMockParser());
        return i;
    }
}
