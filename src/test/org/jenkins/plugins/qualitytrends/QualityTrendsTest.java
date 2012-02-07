package org.jenkins.plugins.qualitytrends;

import com.google.common.collect.Sets;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;
import org.jenkins.plugins.qualitytrends.model.Severity;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Emanuele Zattin
 */
public class QualityTrendsTest extends HudsonTestCase {

    public void test() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new Shell("echo hello"));
        project.getPublishersList().add(new QualityTrends(createParserIterable()));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

    }
    
    private Parser createMockParser() {
        Parser parser = mock(Parser.class);
        when(parser.getRegex())
                .thenReturn("^.*$");
        when(parser.getParserResult(any(Matcher.class)))
                .thenReturn(new ParserResult(
                        "Mock Parser",
                        Severity.INFO,
                        new File("."),
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
