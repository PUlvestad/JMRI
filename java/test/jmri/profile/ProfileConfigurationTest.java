package jmri.profile;

import java.io.File;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProfileConfigurationTest {

    @Test
    public void testCTor(@TempDir File folder) throws java.io.IOException {
        File profileFolder = new File(folder, "test");
        Profile instance = new Profile("test", "test", profileFolder);
        instance.setName("saved");

        ProfileConfiguration t = new ProfileConfiguration(instance);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProfileConfigurationTest.class);

}
