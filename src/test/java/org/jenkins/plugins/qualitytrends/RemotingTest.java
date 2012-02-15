package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.LineReader;
import com.google.common.io.Resources;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.remoting.VirtualChannel;
import hudson.slaves.DumbSlave;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;
import org.jenkins.plugins.qualitytrends.model.Severity;
import org.jenkins.plugins.qualitytrends.util.DirectoryScanner;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

/**
 * @author Emanuele Zattin
 */

public class RemotingTest extends HudsonTestCase {


    public void test() {
        try {
            // Set up the slave
            DumbSlave slave = createSlave();

            // Set up the job and assign it to the slave
            FreeStyleProject project = createFreeStyleProject();
            project.setAssignedNode(slave);
            project.setScm(new ExtractResourceSCM(Resources.getResource("jenkinsci-groovyaxis.zip")));
            project.getBuildersList().add(new MyTestBuilder());
            project.getPublishersList().add(new QualityTrends(Sets.newHashSet(Arrays.<Parser>asList(new MyParser()))));

            // Start a build
            FreeStyleBuild build = project.scheduleBuild2(0).get();
            System.out.println("Build " + build + " ended");

            // Show the console log
            LineReader lineReader = new LineReader(build.getLogReader());
            String line = lineReader.readLine();
            while (line != null) {
                System.out.println("> " + line);
                line = lineReader.readLine();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return;
        }

    }
    
    public static class MyTestBuilder extends TestBuilder{
        @Override
        public boolean perform(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
            Iterable<String> fileAbsolutePaths = abstractBuild.getWorkspace().act(new TestFileCallable());

            for (String fileAbsolutePath : fileAbsolutePaths) {
                buildListener.getLogger().println(pipeSeparatedString(fileAbsolutePath));
            }

            return true;
        }

        public String pipeSeparatedString(String path) {
            List<String> s = Lists.newArrayList();
            s.add(path);
            s.add("1");
            s.add("MockParser");
            s.add("WARNING");
            s.add("Mock Message");

            return Joiner.on("|").join(s);
        }
    }    

    public static class TestFileCallable implements FilePath.FileCallable<HashSet<String>> {

        public HashSet<String> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            Iterable<File> files = DirectoryScanner
                    .forDirectory(f)
                    .restrictDirectories(Predicates.<File>alwaysTrue())
                    .restrictFiles(Predicates.<File>alwaysTrue())
                    .getFiles();

            return Sets.newHashSet(Iterables.transform(files, new AbsolutePathFunction()));
        }

        public class AbsolutePathFunction implements Function<File, String> {
            public String apply(File file) {
                return file.getAbsolutePath();
            }
        }
    }
    
    public static class MyParser extends Parser {

        public String getName() {
            return "MockParser";
        }

        public String getRegex() {
            return "^(.+)[|](.+)[|](.+)[|](.+)[|](.+)$";
        }

        public ParserResult getParserResult(Matcher matcher) {
            return new ParserResult(
                    "MockParser",
                    Severity.WARNING,
                    matcher.group(1),
                    Integer.parseInt(matcher.group(2)),
                    matcher.group(5)
            );
        }
    }
}
