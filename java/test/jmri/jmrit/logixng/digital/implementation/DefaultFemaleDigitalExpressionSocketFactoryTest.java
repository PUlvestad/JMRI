package jmri.jmrit.logixng.digital.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalExpressionSocketFactoryTest {

    @Test
    public void testCtor() {
        DefaultFemaleDigitalExpressionSocketFactory b = new DefaultFemaleDigitalExpressionSocketFactory();
        Assert.assertNotNull("exists", b);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
