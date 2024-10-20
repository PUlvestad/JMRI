package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBeanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a Memory in a JSpinner.
 * <P>
 * Handles the case of either a String or an Integer in the Memory, preserving
 * what it finds.
 * <P>
 * @author Bob Jacobsen Copyright (c) 2009
 * @version $Revision$
 * @since 2.7.2
 */
public class MemorySpinnerIcon extends PositionableJPanel implements ChangeListener, PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 258551284293568574L;
    int _min = 0;
    int _max = 100;
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, _min, _max, 1));
    // the associated Memory object
    //Memory memory = null;    
    private NamedBeanHandle<Memory> namedMemory;

    public MemorySpinnerIcon(Editor editor) {
        super(editor);
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(spinner, new java.awt.GridBagConstraints());
        spinner.addChangeListener(this);
        javax.swing.JTextField textBox = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        textBox.addMouseMotionListener(this);
        textBox.addMouseListener(this);
        setPopupUtility(new PositionablePopupUtil(this, textBox));
    }

    public Positionable deepClone() {
        MemorySpinnerIcon pos = new MemorySpinnerIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        MemorySpinnerIcon pos = (MemorySpinnerIcon) p;
        pos.setMemory(namedMemory.getName());
        return super.finishClone(pos);
    }
    public javax.swing.JComponent getTextComponent() {
        return ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
    }

    public Dimension getSize() {
        if (debug) {
            Dimension d = spinner.getSize();
            if (debug) {
                log.debug("spinner width= " + d.width + ", height= " + d.height);
            }
            java.awt.Rectangle rect = getBounds(null);
            if (debug) {
                log.debug("Bounds rect= (" + rect.x + "," + rect.y
                        + ") width= " + rect.width + ", height= " + rect.height);
            }
            d = super.getSize();
            if (debug) {
                log.debug("Panel width= " + d.width + ", height= " + d.height);
            }
        }
        return super.getSize();
    }

    /**
     * Attached a named Memory to this display item
     *
     * @param pName Used as a system/user name to lookup the Memory object
     */
    public void setMemory(String pName) {
        if (debug) {
            log.debug("setMemory for memory= " + pName);
        }
        if (InstanceManager.memoryManagerInstance() != null) {
            Memory memory = InstanceManager.memoryManagerInstance().
                    provideMemory(pName);
            if (memory != null) {
                setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
            } else {
                log.error("Memory '" + pName + "' not available, icon won't see changes");
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named Memory to this display item
     *
     * @param m The Memory object
     */
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        namedMemory = m;
        if (namedMemory != null) {
            getMemory().addPropertyChangeListener(this, namedMemory.getName(), "Memory Spinner Icon");
            displayState();
            setName(namedMemory.getName());
        }
    }

    public NamedBeanHandle<Memory> getNamedMemory() {
        return namedMemory;
    }

    // update icon as state of Memory changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    public Memory getMemory() {
        if (namedMemory == null) {
            return null;
        }
        return namedMemory.getBean();
    }

    public void stateChanged(ChangeEvent e) {
        spinnerUpdated();
    }

    public String getNameString() {
        String name;
        if (namedMemory == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getMemory().getUserName() != null) {
            name = getMemory().getUserName() + " (" + getMemory().getSystemName() + ")";
        } else {
            name = getMemory().getSystemName();
        }
        return name;
    }
    /*
     public void setSelectable(boolean b) {selectable = b;}
     public boolean isSelectable() { return selectable;}
     boolean selectable = false;
     */

    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Memory"));
        popup.add(new AbstractAction(txt) {
            /**
             *
             */
            private static final long serialVersionUID = 5789214650725618235L;

            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    protected void edit() {
        makeIconEditorFrame(this, "Memory", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editMemory();
            }
        };
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getMemory());
    }

    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * Drive the current state of the display from the state of the Memory.
     */
    public void displayState() {
        if (debug) {
            log.debug("displayState");
        }
        if (namedMemory == null) {  // leave alone if not connected yet
            return;
        }
        if (getMemory().getValue() == null) {
            return;
        }
        Integer num = null;
        if (getMemory().getValue().getClass() == String.class) {
            try {
                num = Integer.valueOf((String) getMemory().getValue());
            } catch (NumberFormatException e) {
                return;
            }
        } else if (getMemory().getValue().getClass() == Integer.class) {
            num = ((Number) getMemory().getValue()).intValue();
        } else if (getMemory().getValue().getClass() == Float.class) {
            num = Integer.valueOf(Math.round((Float) getMemory().getValue()));
            if (debug) {
                log.debug("num= " + num.toString());
            }
        } else {
            //spinner.setValue(getMemory().getValue());
            return;
        }
        int n = num.intValue();
        if (n > _max) {
            num = Integer.valueOf(_max);
        } else if (n < _min) {
            num = Integer.valueOf(_min);
        }
        spinner.setValue(num);
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        spinnerUpdated();
        super.mouseExited(e);
    }

    protected void spinnerUpdated() {
        if (namedMemory == null) {
            return;
        }
        if (getMemory().getValue() == null) {
            getMemory().setValue(spinner.getValue());
            return;
        }
        // Spinner is always an Integer, but memory can contain Integer or String
        if (getMemory().getValue().getClass() == String.class) {
            String newValue = "" + spinner.getValue();
            if (!getMemory().getValue().equals(newValue)) {
                getMemory().setValue(newValue);
            }
        } else {
            getMemory().setValue(spinner.getValue());
        }
    }

    public String getValue() {
        return "" + spinner.getValue();
    }

    void cleanup() {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (spinner != null) {
            spinner.removeChangeListener(this);
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().removeMouseMotionListener(this);
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().removeMouseListener(this);
        }
        spinner = null;
        namedMemory = null;
    }

    private final static Logger log = LoggerFactory.getLogger(MemorySpinnerIcon.class.getName());
}
