package jmri.jmrix.grapevine.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.grapevine.SerialSensorManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring SerialSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007
 * @version $Revision$
 */
public class SerialSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public SerialSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.grapevine.configurexml.SerialSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // create the master object
        SerialSensorManager.instance();
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManagerXml.class.getName());
}
