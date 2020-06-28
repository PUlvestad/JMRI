package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VirtualSignalMastTest {

    @Test
    public void testCTor() {
        VirtualSignalMast t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
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

    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalMastTest.class);

}
