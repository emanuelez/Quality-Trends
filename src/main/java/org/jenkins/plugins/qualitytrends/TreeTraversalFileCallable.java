package org.jenkins.plugins.qualitytrends;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * A FilePath.FileCallable that, given a set of relative paths returns
 * a map containing the relative and absolute paths.
 *
 * @author Emanuele Zattin
 */

public class TreeTraversalFileCallable implements FilePath.FileCallable<Map<String, String>> {

    private Set<String> fileNames;
    private Map<String, String> result = Maps.newHashMap();

    /**
     * Constructor
     * @param fileNames the set of relatives file names.
     */
    public TreeTraversalFileCallable(Set<String> fileNames) {
        this.fileNames = fileNames;
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

        Stack<File> toVisit = new Stack<File>();
        toVisit.push(root);

        while (!toVisit.isEmpty()) {
            File currentNode = toVisit.pop();
            System.out.println(currentNode + " : " + currentNode.isDirectory());
            if (currentNode.isDirectory()) {
                for (File child : currentNode.listFiles()) {
                    toVisit.push(child);
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
