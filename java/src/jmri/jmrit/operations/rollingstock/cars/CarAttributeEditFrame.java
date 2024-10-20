// CarAttributeEditFrame.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationsByCarTypeFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainsByCarTypeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -8817198567309887518L;

    CarManager carManager = CarManager.instance();

    // labels
    JLabel textAttribute = new JLabel();
    JLabel textSep = new JLabel();
    JLabel quanity = new JLabel("0");

    // major buttons
    JButton addButton = new JButton();
    JButton deleteButton = new JButton();
    JButton replaceButton = new JButton();

    // combo box
    JComboBox<String> comboBox;

    // text box
    JTextField addTextBox = new JTextField(Control.max_len_string_attibute);

    // property change
    public static final String DISPOSE = "dispose"; // NOI18N

    public CarAttributeEditFrame() {
    }

    String _comboboxName; // used to determine which combo box is being edited

    public void initComponents(String comboboxName) {
        initComponents(comboboxName, NONE);
    }

    public void initComponents(String comboboxName, String select) {

        getContentPane().removeAll();

        setTitle(MessageFormat.format(Bundle.getMessage("TitleCarEditAtrribute"), new Object[]{comboboxName}));

        // track which combo box is being edited
        _comboboxName = comboboxName;
        loadCombobox();
        comboBox.setSelectedItem(select);

        // general GUI config
        getContentPane().setLayout(new GridBagLayout());

        textAttribute.setText(comboboxName);

        addButton.setText(Bundle.getMessage("Add"));
        addButton.setVisible(true);
        deleteButton.setText(Bundle.getMessage("Delete"));
        deleteButton.setVisible(true);
        replaceButton.setText(Bundle.getMessage("Replace"));
        replaceButton.setVisible(true);

        quanity.setVisible(showQuanity);

        // row 1
        addItem(textAttribute, 2, 1);
        // row 2
        addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);

        // row 3
        addItem(quanity, 1, 3);
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);

        // row 4
        addItem(replaceButton, 3, 4);

        addButtonAction(addButton);
        addButtonAction(deleteButton);
        addButtonAction(replaceButton);

        addComboBoxAction(comboBox);
        carManager.addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        toolMenu.add(new CarAttributeAction(Bundle.getMessage("CarQuanity"), this));
        toolMenu.add(new CarDeleteAttributeAction(Bundle.getMessage("DeleteUnusedAttributes"), this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditCarAttributes", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight250));

    }

    // add, delete, or replace button
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("edit frame button activated");
        if (ae.getSource() == addButton) {
            String addItem = addTextBox.getText().trim();
            if (addItem.equals(NONE)) {
                return;
            }
            String[] item = {addItem};
            if (_comboboxName == CarEditFrame.TYPE) {
                item = addItem.split("-");
            }
            if (item[0].length() > Control.max_len_string_attibute) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                        new Object[]{Control.max_len_string_attibute}), MessageFormat.format(Bundle
                                .getMessage("canNotAdd"), new Object[]{_comboboxName}), JOptionPane.ERROR_MESSAGE);
                return;
            }
            addItemToCombobox(addItem);
        }
        if (ae.getSource() == deleteButton) {
            deleteItemFromCombobox((String) comboBox.getSelectedItem());
        }
        if (ae.getSource() == replaceButton) {
            String newItem = addTextBox.getText().trim();
            if (newItem.equals(NONE)) {
                return;
            }
            String[] item = {newItem};
            if (_comboboxName == CarEditFrame.TYPE) {
                item = newItem.split("-");
            }
            if (item[0].length() > Control.max_len_string_attibute) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                        new Object[]{Control.max_len_string_attibute}), MessageFormat.format(Bundle
                                .getMessage("canNotReplace"), new Object[]{_comboboxName}), JOptionPane.ERROR_MESSAGE);
                return;
            }
            String oldItem = (String) comboBox.getSelectedItem();
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("replaceMsg"), new Object[]{
                oldItem, newItem}), Bundle.getMessage("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            if (newItem.equals(oldItem)) {
                return;
            }
            // need to make sure locations and trains are loaded
            TrainManager.instance();
            // LocationManager.instance();
            // don't show dialog, save current state
            boolean oldShow = showDialogBox;
            showDialogBox = false;
            addItemToCombobox(newItem);
            showDialogBox = oldShow;
            replaceItem(oldItem, newItem);
            deleteItemFromCombobox(oldItem);
        }
    }

    protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("Combo box action");
        updateCarQuanity();
    }

    private void deleteItemFromCombobox(String deleteItem) {
        log.debug("delete attribute {}", deleteItem);
        if (_comboboxName == CarEditFrame.ROAD) {
            // purge train and locations by using replace
            CarRoads.instance().replaceName(deleteItem, null);
        }
        if (_comboboxName == CarEditFrame.TYPE) {
            CarTypes.instance().deleteName(deleteItem);
        }
        if (_comboboxName == CarEditFrame.COLOR) {
            CarColors.instance().deleteName(deleteItem);
        }
        if (_comboboxName == CarEditFrame.LENGTH) {
            CarLengths.instance().deleteName(deleteItem);
        }
        if (_comboboxName == CarEditFrame.OWNER) {
            CarOwners.instance().deleteName(deleteItem);
        }
        if (_comboboxName == CarEditFrame.KERNEL) {
            carManager.deleteKernel(deleteItem);
        }
    }

    static boolean showDialogBox = true;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    private void addItemToCombobox(String addItem) {
        if (_comboboxName == CarEditFrame.ROAD) {
            CarRoads.instance().addName(addItem);
        }
        if (_comboboxName == CarEditFrame.TYPE) {
            CarTypes.instance().addName(addItem);
            if (showDialogBox) {
                int results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"), Bundle
                        .getMessage("ModifyLocations"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonDontShow")}, Bundle.getMessage("ButtonNo"));
                if (results == JOptionPane.YES_OPTION) {
                    LocationsByCarTypeFrame lf = new LocationsByCarTypeFrame();
                    lf.initComponents(addItem);
                }
                if (results == JOptionPane.CANCEL_OPTION) {
                    showDialogBox = false;
                }
                results = JOptionPane.showOptionDialog(this, Bundle.getMessage("AddNewCarType"), Bundle
                        .getMessage("ModifyTrains"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonDontShow")}, Bundle.getMessage("ButtonNo"));
                if (results == JOptionPane.YES_OPTION) {
                    TrainsByCarTypeFrame lf = new TrainsByCarTypeFrame();
                    lf.initComponents(addItem);
                }
                if (results == JOptionPane.CANCEL_OPTION) {
                    showDialogBox = false;
                }
            }
        }
        if (_comboboxName == CarEditFrame.COLOR) {
            CarColors.instance().addName(addItem);
        }
        if (_comboboxName == CarEditFrame.LENGTH) {
            // convert from inches to feet if needed
            if (addItem.endsWith("\"")) { // NOI18N
                addItem = addItem.substring(0, addItem.length() - 1);
                try {
                    double inches = Double.parseDouble(addItem);
                    int feet = (int) (inches * Setup.getScaleRatio() / 12);
                    addItem = Integer.toString(feet);
                } catch (NumberFormatException e) {
                    log.error("can not convert from inches to feet");
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertFeet"), Bundle
                            .getMessage("ErrorCarLength"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (addItem.endsWith("cm")) { // NOI18N
                addItem = addItem.substring(0, addItem.length() - 2);
                try {
                    double cm = Double.parseDouble(addItem);
                    int meter = (int) (cm * Setup.getScaleRatio() / 100);
                    addItem = Integer.toString(meter);
                } catch (NumberFormatException e) {
                    log.error("Can not convert from cm to meters");
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertMeter"), Bundle
                            .getMessage("ErrorCarLength"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // confirm that length is a number and less than 10000 feet
            try {
                int length = Integer.parseInt(addItem);
                if (length < 0) {
                    log.error("length ({}) has to be a positive number", addItem);
                    return;
                }
                if (addItem.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carAttribute"),
                            new Object[]{Control.max_len_string_length_name}), MessageFormat.format(Bundle
                                    .getMessage("canNotAdd"), new Object[]{_comboboxName}), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                log.error("length ({}) is not an integer", addItem);
                return;
            }
            CarLengths.instance().addName(addItem);
            comboBox.setSelectedItem(addItem);
        }
        if (_comboboxName == CarEditFrame.KERNEL) {
            carManager.newKernel(addItem);
        }
        if (_comboboxName == CarEditFrame.OWNER) {
            CarOwners.instance().addName(addItem);
        }
    }

    private void replaceItem(String oldItem, String newItem) {
        // replace kernel
        if (_comboboxName == CarEditFrame.KERNEL) {
            carManager.replaceKernelName(oldItem, newItem);
        }
        // now adjust cars, locations and trains
        if (_comboboxName == CarEditFrame.TYPE) {
            CarTypes.instance().replaceName(oldItem, newItem);
            CarLoads.instance().replaceType(oldItem, newItem);
        }
        if (_comboboxName == CarEditFrame.ROAD) {
            CarRoads.instance().replaceName(oldItem, newItem);
        }
        if (_comboboxName == CarEditFrame.OWNER) {
            CarOwners.instance().replaceName(oldItem, newItem);
        }
        if (_comboboxName == CarEditFrame.LENGTH) {
            CarLengths.instance().replaceName(oldItem, newItem);
        }
        if (_comboboxName == CarEditFrame.COLOR) {
            CarColors.instance().replaceName(oldItem, newItem);
        }
    }

    private void loadCombobox() {
        if (_comboboxName == CarEditFrame.ROAD) {
            comboBox = CarRoads.instance().getComboBox();
            CarRoads.instance().addPropertyChangeListener(this);
        }
        if (_comboboxName == CarEditFrame.TYPE) {
            comboBox = CarTypes.instance().getComboBox();
            CarTypes.instance().addPropertyChangeListener(this);
        }
        if (_comboboxName == CarEditFrame.COLOR) {
            comboBox = CarColors.instance().getComboBox();
            CarColors.instance().addPropertyChangeListener(this);
        }
        if (_comboboxName == CarEditFrame.LENGTH) {
            comboBox = CarLengths.instance().getComboBox();
            CarLengths.instance().addPropertyChangeListener(this);
        }
        if (_comboboxName == CarEditFrame.OWNER) {
            comboBox = CarOwners.instance().getComboBox();
            CarOwners.instance().addPropertyChangeListener(this);
        }
        if (_comboboxName == CarEditFrame.KERNEL) {
            comboBox = carManager.getKernelComboBox();
        }
    }

    boolean showQuanity = false;

    public void toggleShowQuanity() {
        if (showQuanity) {
            showQuanity = false;
        } else {
            showQuanity = true;
        }
        quanity.setVisible(showQuanity);
        updateCarQuanity();
    }

    private void updateCarQuanity() {
        if (!showQuanity) {
            return;
        }
        int number = 0;
        String item = (String) comboBox.getSelectedItem();
        log.debug("Selected item {}", item);
        for (RollingStock rs : carManager.getList()) {
            Car car = (Car) rs;

            if (_comboboxName == CarEditFrame.ROAD) {
                if (car.getRoadName().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName == CarEditFrame.TYPE) {
                if (car.getTypeName().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName == CarEditFrame.COLOR) {
                if (car.getColor().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName == CarEditFrame.LENGTH) {
                if (car.getLength().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName == CarEditFrame.OWNER) {
                if (car.getOwner().equals(item)) {
                    number++;
                }
            }
            if (_comboboxName == CarEditFrame.KERNEL) {
                if (car.getKernelName().equals(item)) {
                    number++;
                }
            }
        }
        quanity.setText(Integer.toString(number));
        // Tool to delete all attributes that haven't been assigned to a car
        if (number == 0 && deleteUnused) {
            // need to check if an engine is using the road name
            if (_comboboxName == CarEditFrame.ROAD) {
                for (RollingStock rs : EngineManager.instance().getList()) {
                    if (rs.getRoadName().equals(item)) {
                        log.info("Engine (" + rs.getRoadName() + " " + rs.getNumber()
                                + ") has assigned road name (" + item + ")"); // NOI18N
                        return;
                    }
                }
            }
            // confirm that attribute is to be deleted
            if (!cancel) {
                int results = JOptionPane.showOptionDialog(null, MessageFormat.format(Bundle
                        .getMessage("ConfirmDeleteAttribute"), new Object[]{_comboboxName, item}), Bundle
                        .getMessage("DeleteAttribute?"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, new Object[]{Bundle.getMessage("ButtonYes"),
                            Bundle.getMessage("ButtonNo"), Bundle.getMessage("ButtonCancel")}, Bundle
                        .getMessage("ButtonYes"));
                if (results == JOptionPane.YES_OPTION) {
                    deleteItemFromCombobox((String) comboBox.getSelectedItem());
                }
                if (results == JOptionPane.CANCEL_OPTION || results == JOptionPane.CLOSED_OPTION) {
                    cancel = true;
                }
            }
        }
    }

    boolean deleteUnused = false;
    boolean cancel = false;

    public void deleteUnusedAttribures() {
        if (!showQuanity) {
            toggleShowQuanity();
        }
        deleteUnused = true;
        cancel = false;
        int items = comboBox.getItemCount() - 1;
        for (int i = items; i >= 0; i--) {
            comboBox.setSelectedIndex(i);
        }
        deleteUnused = false; // done
        comboBox.setSelectedIndex(0); // update count
    }

    public void dispose() {
        CarRoads.instance().removePropertyChangeListener(this);
        CarTypes.instance().removePropertyChangeListener(this);
        CarColors.instance().removePropertyChangeListener(this);
        CarLengths.instance().removePropertyChangeListener(this);
        CarOwners.instance().removePropertyChangeListener(this);
        carManager.removePropertyChangeListener(this);
        firePcs(DISPOSE, _comboboxName, null);
        super.dispose();
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            CarRoads.instance().updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)) {
            CarTypes.instance().updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)) {
            CarColors.instance().updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)) {
            CarLengths.instance().updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            CarOwners.instance().updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarManager.KERNEL_LISTLENGTH_CHANGED_PROPERTY)) {
            carManager.updateKernelComboBox(comboBox);
        }
        if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateCarQuanity();
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // note firePropertyChange occurs during frame creation
    private void firePcs(String p, Object old, Object n) {
        log.debug("CarAttribute firePropertyChange {}", p);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarAttributeEditFrame.class.getName());
}
