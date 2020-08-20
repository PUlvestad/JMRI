package jmri.jmrit.ctc.ctcserialdata;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.*;
import jmri.jmrit.ctc.*;

/**
 * This describes a single line of Call On data.  The list of call on rules
 * for each OS section are in the _mCO_GroupingsList variable in {@link CodeButtonHandlerData}.
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CallOnData {
    public NBHSignal _mExternalSignal;
    public String _mSignalFacingDirection;
    public String _mSignalAspectToDisplay;
    public NBHSensor _mCalledOnExternalSensor;
    public Block _mExternalBlock;
    public List<NBHSensor> _mSwitchIndicators;      // Up to 6 entries

    public CallOnData() {
    }

    public CallOnData( NBHSignal externalSignal,
                        String signalFacingDirection,
                        String signalAspectToDisplay,
                        NBHSensor calledOnExternalSensor,
                        Block externalBlock,
                        ArrayList<NBHSensor> switchIndicators) {
        _mExternalSignal = externalSignal;
        _mSignalFacingDirection = signalFacingDirection;
        _mSignalAspectToDisplay = signalAspectToDisplay;
        _mCalledOnExternalSensor = calledOnExternalSensor;
        _mExternalBlock = externalBlock;
        _mSwitchIndicators = switchIndicators;
    }

    public String toString() {
        String formattedString = String.format("%s,%s,%s,%s,%s",
                _mExternalSignal != null ? _mExternalSignal.getHandleName() : "",
                _mSignalFacingDirection != null ? _mSignalFacingDirection : "",
                _mSignalAspectToDisplay != null ? _mSignalAspectToDisplay : "",
                _mCalledOnExternalSensor != null ? _mCalledOnExternalSensor.getHandleName() : "",
                _mExternalBlock != null ? _mExternalBlock.getDisplayName() : "");
        StringBuilder buildString = new StringBuilder(formattedString);
        _mSwitchIndicators.forEach(sw -> {
            buildString.append(",");
            buildString.append(sw.getHandleName());
        });
        return buildString.toString();
    }
}
