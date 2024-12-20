package jmri.managers.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring
 * InternalReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision$
 */
public class InternalReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public InternalReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual reporters
        return loadReporters(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(InternalReporterManagerXml.class.getName());
}
