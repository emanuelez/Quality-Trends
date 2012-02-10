package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A FilePath.FileCallable that, given a set of relative paths returns
 * a map containing the relative and absolute paths.
 *
 * @author Emanuele Zattin
 */

public class TreeTraversalFileCallable implements FilePath.FileCallable<Map<String, String>> {

    private Set<String> fileNames;
    private int maxLevel;
    private Map<String, String> result = Maps.newHashMap();

    /**
     * Constructor
     * @param fileNames the set of relatives file names.
     */
    public TreeTraversalFileCallable(Set<String> fileNames, int maxLevel) {
        this.fileNames = fileNames;
        this.maxLevel = maxLevel;
    }

    /**
     *
     * @param root the work space folder
     * @param channel provided by Jenkins
     * @return a map containing the relative and absolute paths.
     * The map is not guaranteed to have the same size of the provided set,
     * since some files might be impossible to find.
     * @throws IOException
     * @throws InterruptedException
     */
    public Map<String, String> invoke(File root, VirtualChannel channel) throws IOException, InterruptedException {
        
        int level = 0;
        File nextLevelJump = root;
        boolean flagJump = false;

        Queue<File> toVisit = new LinkedList<File>();
        toVisit.add(root);

        while (!toVisit.isEmpty()) {
            if (level >= maxLevel) {
                return result;
            }
            File currentNode = toVisit.poll();
            if(currentNode == nextLevelJump) {
                level++;
                flagJump = true;
            }
            if (currentNode.isDirectory()) {
                for (File child : currentNode.listFiles()) {
                    if(flagJump) {
                        flagJump = false;
                        nextLevelJump = child;
                    }
                    toVisit.add(child);
                }
            } else if (currentNode.isFile()) {
                compute(currentNode);
            }
        }
        
        return result;
    }

    private void compute(File currentNode) {
        String absolutePath = currentNode.getAbsolutePath();
        try {
            String relativePath = Iterables.find(fileNames, new EndsWith(absolutePath));
            result.put(relativePath, absolutePath);
        } catch (NoSuchElementException e) {
            return;
        }
    }
    
    private class EndsWith implements Predicate<String> {
        
        private String absolutePath;

        private EndsWith(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        public boolean apply(String s) {
            return s != null && absolutePath.endsWith(s);
        }
    }
}