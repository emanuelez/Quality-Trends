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
        int buildNumber = 1;

        int entry_id;
        try {
            entry_id = controller.addEntry(
                    buildNumber,
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
            controller.associateFileSha1ToEntryForBuildAndFileName(buildNumber, "test.txt", "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3");
        } catch (SQLException e) {
            fail("Could not add the file SHA1 to an entry");
            return;
        }

        String warning_id = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";

        try {
            controller.associateWarningToEntry(warning_id, entry_id);
        } catch (SQLException e) {
            fail("Could not associate the warning id to an entry");
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        controller.tearDownDb();
    }
}