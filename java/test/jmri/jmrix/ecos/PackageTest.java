package jmri.jmrix.ecos;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ecos package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.ecos.PackageTest");  // no tests in this class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrix.ecos.swing.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ecos.networkdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ecos.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EcosPreferencesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EcosSystemConnectionMemoTest.class));
        if (!System.getProperty("jmri.headlesstest", System.getProperty("java.awt.headless", "false")).equals("true")) {
        }

        return suite;
    }

}
