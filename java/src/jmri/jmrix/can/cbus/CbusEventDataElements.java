package jmri.jmrix.can.cbus;

import javax.annotation.Nonnull;
import jmri.jmrix.can.CanMessage;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to enable storage and OPC calculation
 * according to CBUS Event Data.
 * 
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventDataElements {
    
    private int _dat1;
    private int _dat2;
    private int _dat3;
    private int _numElements;
    
    /**
     * ENUM of the event state.
     * <p>
     * Events generally have on, off or unknown status.
     * <p>
     * They can also be asked to request their current status via the network,
     * or toggled to the opposite state that it is currently at.
     */
    public enum EvState{
        ON, OFF, UNKNOWN, REQUEST, TOGGLE;
    }
    
    /**
     * Create Data Elements for a CBUS Event
     */
    public CbusEventDataElements(){
        _numElements = 0;
    }
    
    /**
     * 
     * @param canId
     * @param nn
     * @param en
     * @param state
     * @return ready to send CanMessage
     */
    public CanMessage getCanMessage(int canId, int nn, int en, @Nonnull EvState state){
    
        CanMessage m = new CanMessage(canId);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        m.setElement(3, en >> 8);
        m.setElement(4, en & 0xff);
        
        switch (state) {
            case ON:
                setCanMessageData(m);
                return(finishOnEvent(m,nn>0));
            case OFF:
                setCanMessageData(m);
                return(finishOffEvent(m,nn>0));
            default:
                return(finishRequest(m,nn>0));
        }
    }
    
    private void setCanMessageData( CanMessage m) {
        m.setNumDataElements(5+_numElements);
        switch (_numElements) {
            case 1:
                m.setElement(5, _dat1);
                break;
            case 2:
                m.setElement(5, _dat1);
                m.setElement(6, _dat2);
                break;
            case 3:
                m.setElement(5, _dat1);
                m.setElement(6, _dat2);
                m.setElement(7, _dat3);
                break;
            default:
                break;
        }
    }
    
    private CanMessage finishOnEvent( CanMessage m, boolean isLong) {
        int opc;
        switch (_numElements) {
            case 1:
                opc = (isLong ? CbusConstants.CBUS_ACON1 : CbusConstants.CBUS_ASON1);
                break;
            case 2:
                opc = (isLong ? CbusConstants.CBUS_ACON2 : CbusConstants.CBUS_ASON2);
                break;
            case 3:
                opc = (isLong ? CbusConstants.CBUS_ACON3 : CbusConstants.CBUS_ASON3);
                break;
            default:
                opc = (isLong ? CbusConstants.CBUS_ACON : CbusConstants.CBUS_ASON);
                break;
        }
        m.setElement(0, opc);
        return m;
    }
    
    private CanMessage finishOffEvent( CanMessage m, boolean isLong) {
        int opc;
        switch (_numElements) {
            case 1:
                opc = (isLong ? CbusConstants.CBUS_ACOF1 : CbusConstants.CBUS_ASOF1);
                break;
            case 2:
                opc = (isLong ? CbusConstants.CBUS_ACOF2 : CbusConstants.CBUS_ASOF2);
                break;
            case 3:
                opc = (isLong ? CbusConstants.CBUS_ACOF3 : CbusConstants.CBUS_ASOF3);
                break;
            default:
                opc = (isLong ? CbusConstants.CBUS_ACOF : CbusConstants.CBUS_ASOF);
                break;
        }
        m.setElement(0, opc);
        return m;
    }
    
    private CanMessage finishRequest( CanMessage m, boolean isLong) {
        m.setNumDataElements(5);
        if (isLong) {
            m.setElement(0, CbusConstants.CBUS_AREQ);
        } else {
            m.setElement(0, CbusConstants.CBUS_ASRQ);
        }
        return m;
    }
    
    /**
     * Set Number of Event Data Elements (bytes).
     * @param elements 0-3
     */
    public void setNumElements(int elements){
        if (elements<0 || elements > 3){
            throw new IllegalArgumentException("" + elements + " Event Data Elements Invalid");
        }
        _numElements = elements;
    }
    
    /**
     * Get Number of Event Data Elements (bytes).
     * @return Number of Data Bytes
     */
    public int getNumElements() {
        return _numElements;
    }
    
    /**
     * Set value of a single event Data Byte.
     * @param index Event Index: 1, 2 or 3
     * @param value Byte value 0-255
     */
    public void setData(int index, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Data Value " + value + " Invalid");
        }
        switch (index) {
            case 1:
                _dat1 = value;
                break;
            case 2:
                _dat2 = value;
                break;
            case 3:
                _dat3 = value;
                break;
            default:
                throw new IllegalArgumentException("Data Index " + index + " Invalid");
        }
    }
    
    /**
     * Get value of a single event Data Byte.
     * @param index Event Index: 1, 2 or 3
     * @return Byte value 0-255
     */
    public int getData(int index) {
        switch (index) {
            case 1:
                return _dat1;
            case 2:
                return _dat2;
            case 3:
                return _dat3;
            default:
                throw new IllegalArgumentException("Data Index " + index + " Invalid");
        }
    }
    
    // private static final Logger log = LoggerFactory.getLogger(CbusEventDataElements.class);

}
