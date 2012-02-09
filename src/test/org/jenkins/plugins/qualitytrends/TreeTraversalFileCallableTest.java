package org.jenkins.plugins.qualitytrends;

import com.google.common.collect.Sets;
import hudson.remoting.VirtualChannel;
import junit.framework.TestCase;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Emanuele Zattin
 */

public class TreeTraversalFileCallableTest extends TestCase {
    
    private File root;

    public void setUp() {
        // Set up some fake files and folders to play with

        /**
         * Create this fake fs:
         *
         * root
         *  + child1
         *  + child2
         *  | + childa
         *  |    + leaf2
         *  + leaf1
         */

        root = mock(File.class);
        File child1 = mock(File.class);
        File child2 = mock(File.class);
        File childa = mock(File.class);
        File leaf1 = mock(File.class);
        File leaf2 = mock(File.class);
        
        when(root.isFile()).thenReturn(false);
        when(child1.isFile()).thenReturn(false);
        when(child2.isFile()).thenReturn(false);
        when(childa.isFile()).thenReturn(false);
        when(leaf1.isFile()).thenReturn(true);
        when(leaf2.isFile()).thenReturn(true);
        
        when(root.isDirectory()).thenReturn(true);
        when(child1.isDirectory()).thenReturn(true);
        when(child2.isDirectory()).thenReturn(true);
        when(childa.isDirectory()).thenReturn(true);
        when(leaf1.isDirectory()).thenReturn(false);
        when(leaf2.isDirectory()).thenReturn(false);

        when(root.listFiles()).thenReturn(new File[] {child1, child2, leaf1});
        when(child1.listFiles()).thenReturn(new File[] {});
        when(child2.listFiles()).thenReturn(new File[] {childa});
        when(childa.listFiles()).thenReturn(new File[] {leaf2});

        when(leaf1.getAbsolutePath()).thenReturn("/absolute/root/leaf1");
        when(leaf2.getAbsolutePath()).thenReturn("/absolute/root/child2/childa/leaf2");
    }

    public void testInvoke() throws Exception {
        Set<String> relative = Sets.newHashSet();
        relative.add("leaf1");
        relative.add("child2/childa/leaf2");

        TreeTraversalFileCallable callable = new TreeTraversalFileCallable(relative);
        Map<String,String> result = callable.invoke(root, mock(VirtualChannel.class));

        assertTrue("/absolute/root/leaf1".equals(result.get("leaf1")));
        assertTrue("/absolute/root/child2/childa/leaf2".equals(result.get("child2/childa/leaf2")));
    }
}
