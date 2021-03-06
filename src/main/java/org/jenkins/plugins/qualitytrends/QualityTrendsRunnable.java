package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.tasks.Publisher;
import org.jenkins.plugins.qualitytrends.model.Parser;
import org.jenkins.plugins.qualitytrends.model.QualityTrendsException;
import org.jenkins.plugins.qualitytrends.model.BuildStorageManager;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Emanuele Zattin
 */
public class QualityTrendsRunnable implements Runnable {

    private AbstractBuild build;
    @Inject
    private BuildStorageManager storage;

    public QualityTrendsRunnable(AbstractBuild build, BuildStorageManager storage) {
        this.build = build;
        this.storage = storage;
    }

    public void run() {

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

        Map<Parser, Pattern> patterns = Maps.newHashMap();

        for (Parser parser : qualityTrends.getParsers()) {
            patterns.put(parser, Pattern.compile(parser.getRegex()));
        }


        try {
            LineNumberReader logReader = new LineNumberReader(new FileReader(build.getLogFile()));
            String line;

            line = logReader.readLine();
            while (true) {
                if (line == null && !build.isBuilding()) {
                    logReader.close();
                    break;
                }
                if (line == null) {
                    Thread.sleep(1000);
                } else {
                    for (Map.Entry<Parser, Pattern> entry : patterns.entrySet()) {
                        Matcher matcher = entry.getValue().matcher(line);
                        if (matcher.matches()) {
                            storage.addParserResult(entry.getKey().getParserResult(matcher));
                        }
                    }
                }
                line = logReader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (QualityTrendsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
