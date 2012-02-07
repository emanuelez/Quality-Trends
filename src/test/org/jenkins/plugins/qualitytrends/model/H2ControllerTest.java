package org.jenkins.plugins.qualitytrends.model;

import junit.framework.TestCase;

import java.sql.SQLException;

/**
 * @author Emanuele Zattin
 */

public class H2ControllerTest extends TestCase {
    
    private H2Controller controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new H2Controller("h2test");
    }

    public void testDbOperations() {
        int build_id;
        try {
            build_id = controller.addBuild(1);
            assertTrue(build_id > 0);
        } catch (SQLException e) {
            fail("Could not add a build");
            return;
        }

        int entry_id;
        try {
            entry_id = controller.addEntry(
                    build_id,
                    "test.txt",
                    3,
                    "testParser",
                    Severity.WARNING.toString(),
                    null,
                    "testMessage",
                    null);
            assertTrue(entry_id > 0);
        } catch (SQLException e) {
            fail("Could not add an entry");
            return;
        }

        try {
            controller.associateFileSha1ToEntry(entry_id, "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3");
        } catch (SQLException e) {
            fail("Could not add the file SHA1 to an entry");
            return;
        }

        int warning_id;
        try {
            warning_id = controller.addWarning("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12");
            assertTrue(warning_id > 0);
        } catch (SQLException e) {
            fail("Could not create a warning");
            return;
        }

        try {
            controller.associateWarningToEntry(warning_id, entry_id);
        } catch (SQLException e) {
            fail("Could not associate the warning id to an entry");
        }

        try {
            controller.associateCommitToBuild(build_id, "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        } catch (SQLException e) {
            fail("Could not associate a commit to a build");
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        controller.tearDownDb();
    }
}
