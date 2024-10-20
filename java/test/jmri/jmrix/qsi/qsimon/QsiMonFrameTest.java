/**
 * QsiMonFrameTest.java
 *
 * Description:	JUnit tests for the QsiProgrammer class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.qsi.qsimon;

import java.util.Vector;
import jmri.jmrix.qsi.QsiListener;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;
import jmri.jmrix.qsi.QsiTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QsiMonFrameTest extends TestCase {

    public void testCreate() {
        QsiMonFrame f = new QsiMonFrame();
        Assert.assertNotNull("exists", f);
    }

// Following are not reliable, apparently time-sensitive, so commented out
/* 	public void testMsg() { */
    /* 		QsiMessage m = new QsiMessage(3); */
    /* 		m.setOpCode('L'); */
    /* 		m.setElement(1, '0'); */
    /* 		m.setElement(2, 'A'); */
    /*  */
    /* 		QsiMonFrame f = new QsiMonFrame(); */
    /*  */
    /* 		f.message(m); */
    /*  */
    /* 		Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length()); */
    /* 		Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText()); */
    /* 	} */
    /*  */
    /* 	public void testReply() { */
    /* 		QsiReply m = new QsiReply(); */
    /* 		m.setOpCode('C'); */
    /* 		m.setElement(1, 'o'); */
    /* 		m.setElement(2, ':'); */
    /*  */
    /* 		QsiMonFrame f = new QsiMonFrame(); */
    /*  */
    /* 		f.reply(m); */
    /*  */
    /* 		Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText()); */
    /* 		Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length()); */
    /* 	} */
    public void testWrite() {

        // infrastructure objects
        //QsiInterfaceScaffold t = new QsiInterfaceScaffold();
    }

    // service internal class to handle transmit/receive for tests
    class QsiInterfaceScaffold extends QsiTrafficController {

        public QsiInterfaceScaffold() {
        }

        // override some QsiInterfaceController methods for test purposes
        public boolean status() {
            return true;
        }

        /**
         * record messages sent, provide access for making sure they are OK
         */
        public Vector<QsiMessage> outbound = new Vector<QsiMessage>();  // public OK here, so long as this is a test class

        public void sendQsiMessage(QsiMessage m, QsiListener l) {
            if (log.isDebugEnabled()) {
                log.debug("sendQsiMessage [" + m + "]");
            }
            // save a copy
            outbound.addElement(m);
        }

        // test control member functions
        /**
         * forward a message to the listeners, e.g. test receipt
         */
        protected void sendTestMessage(QsiMessage m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestMessage    [" + m + "]");
            }
            notifyMessage(m, null);
            return;
        }

        protected void sendTestReply(QsiReply m) {
            // forward a test message to Listeners
            if (log.isDebugEnabled()) {
                log.debug("sendTestReply    [" + m + "]");
            }
            notifyReply(m);
            return;
        }

        /*
         * Check number of listeners, used for testing dispose()
         */
        public int numListeners() {
            return cmdListeners.size();
        }

    }

    // from here down is testing infrastructure
    public QsiMonFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {QsiMonFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(QsiMonFrameTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(QsiMonFrameTest.class.getName());

}
