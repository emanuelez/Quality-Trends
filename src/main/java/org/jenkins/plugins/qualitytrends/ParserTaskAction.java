package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import hudson.matrix.MatrixProject;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.tasks.Publisher;
import org.jenkins.plugins.qualitytrends.model.Parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserTaskAction extends TaskAction {

    transient private AbstractBuild build;

    public ParserTaskAction(AbstractBuild build, Iterable<Parser> parsers) {
        this.build = build;
        this.workerThread = new ParserTaskThread(this, TaskThread.ListenerAndText.forMemory(this), parsers, build);
        this.workerThread.start();
    }

    @Override
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    protected ACL getACL() {
        return build.getACL();
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    private class ParserTaskThread extends TaskThread {
        
        private Iterable<Parser> parsers;
        private AbstractBuild build;

        private ParserTaskThread(TaskAction owner, ListenerAndText output, Iterable<Parser> parsers, AbstractBuild build) {
            super(owner, output);
            this.parsers = parsers;
            this.build = build;

        }

        @Override
        protected void perform(TaskListener taskListener) throws Exception {

                System.out.println("perform: " + Thread.currentThread().getId());

                Iterable<? extends Publisher> QTPublishers;
                if (build.getParent() instanceof Project) {
                    Project p = (Project) build.getParent();
                    QTPublishers = Iterables.filter(p.getPublishers().values(), Predicates.instanceOf(QualityTrends.class));
                } else if (build.getParent() instanceof MatrixProject) {
                    MatrixProject p = (MatrixProject) build.getParent();
                    QTPublishers = Iterables.filter(p.getPublishers().values(), Predicates.instanceOf(QualityTrends.class));
                } else {
                    return;
                }

                if (Iterables.isEmpty(QTPublishers)) {
                    return;
                }

                QualityTrends qualityTrends = (QualityTrends) Iterables.getOnlyElement(QTPublishers, null);

                Iterable<Parser> parsers = qualityTrends.getParsers();

                Map<Parser, Pattern> patterns = Maps.newHashMap();

                for (Parser parser : parsers) {
                    patterns.put(parser, Pattern.compile(parser.getRegex()));
                }


                try {
                    LineNumberReader logReader = new LineNumberReader(new FileReader(build.getLogFile()));
                    String line;
                    while (true) {
                        line = logReader.readLine();
                        System.out.println(line);
                        while (line == null) {
                            Thread.sleep(1000);
                            line = logReader.readLine();
                        }
                        if (!build.isBuilding()) {
                            System.out.println("Breaking!");
                            logReader.close();
                            break;
                        }


                        for (Parser parser : patterns.keySet()) {
                            Matcher matcher = patterns.get(parser).matcher(line);
                            if (matcher.matches()) {
                                // TODO: Annotate the console
                                System.out.println("Adding to parserResults");
                            }
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
}
