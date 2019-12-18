package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.tools.RouteCopyFrame;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteCopyFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RouteCopyFrame t = new RouteCopyFrame(null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteCopyFrameTest.class);

}
