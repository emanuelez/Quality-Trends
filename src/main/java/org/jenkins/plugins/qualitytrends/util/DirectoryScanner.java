package org.jenkins.plugins.qualitytrends.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public final class DirectoryScanner {

    enum FileTypePredicate implements Predicate<File> {
        FILE {
            public boolean apply(final File input) {
                return input.isFile();
            }
        },
        DIRECTORY {
            public boolean apply(final File input) {
                return input.isDirectory();
            }
        }
    }

    public static DirectoryScanner forDirectory(final File baseDir) {
        return new DirectoryScanner(baseDir);
    }

    public static DirectoryScanner forDirectory(final String baseDir) {
        return forDirectory(new File(baseDir));
    }

    private final File baseDir;

    private Predicate<File> directoryPredicate;

    private Predicate<File> filePredicate;

    private int maxDepth = -1;

    DirectoryScanner(final File baseDir) {
        checkArgument(checkNotNull(baseDir).isDirectory());
        this.baseDir = baseDir;
    }

    public DirectoryScanner restrictDirectories(final Predicate<File> directoryPredicate) {
        this.directoryPredicate =
                Predicates.and(FileTypePredicate.DIRECTORY,
                        checkNotNull(directoryPredicate));
        return this;
    }

    public DirectoryScanner restrictFiles(final Predicate<File> filePredicate) {
        this.filePredicate =
                Predicates.and(FileTypePredicate.FILE, checkNotNull(filePredicate));
        return this;
    }

    public DirectoryScanner maxScanDepth(final int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Iterable<File> getFiles() {
        final Collection<File> data = newLinkedHashSet();
        scan(baseDir, data, 0);
        return data;
    }

    private void scan(
            final File currentDir,
            final Collection<File> holder,
            final int depth) {
        if (maxDepth < 0 || maxDepth >= depth) {
            final List<File> files = Arrays.asList(currentDir.listFiles());
            for (final File subfile : Iterables.filter(files, filePredicate)) {
                holder.add(subfile);
            }
            for (final File subDir : Iterables.filter(files, directoryPredicate)) {
                scan(subDir, holder, depth + 1);
            }
        }
    }

}
