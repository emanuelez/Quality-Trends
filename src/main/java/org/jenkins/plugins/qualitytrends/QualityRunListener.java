package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.ParserResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class QualityRunListener extends RunListener<AbstractBuild> {

    private Set<ParserResult> parserResults = Sets.newHashSet();

    @Override
    public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {

        Iterable<? extends Publisher> QTPublishers;
        if (abstractBuild.getParent() instanceof Project) {
            Project p = (Project) abstractBuild.getParent();
            QTPublishers = Iterables.filter(p.getPublishers().values(), Predicates.instanceOf(QualityTrends.class));
        } else if (abstractBuild.getParent() instanceof MatrixProject) {
            MatrixProject p = (MatrixProject) abstractBuild.getParent();
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
            BufferedReader logReader = new BufferedReader(abstractBuild.getLogReader());
            String line = logReader.readLine();
            while (true) {
                while (line == null) {
                    Thread.sleep(1000);
                }
                if (!abstractBuild.isBuilding()) {
                    logReader.close();
                    break;
                }
                line = logReader.readLine();

                if (line != null) {
                    for (Parser parser : patterns.keySet()) {
                        Matcher matcher = patterns.get(parser).matcher(line);
                        if (matcher.matches()) {
                            // TODO: Annotate the console
                            parserResults.add(parser.getParserResult(matcher));
                        }
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Set<ParserResult> getParserResults() {
        return parserResults;
    }
}
