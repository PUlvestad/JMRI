// CarsSetFrame.java
package jmri.jmrit.operations.rollingstock.cars;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import jmri.util.com.sun.TableSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user to place a group of cars on the layout
 *
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * @version $Revision$
 */
public class CarsSetFrame extends CarSetFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -3962047189679581L;
    CarsTableModel _carsTableModel;
    JTable _carsTable;
    TableSorter _sorter;

    public CarsSetFrame() {
        super();
    }

    // Ignore checkbox states
    private static boolean ignoreStatusCheckBoxSelected = false;
    private static boolean ignoreLocationCheckBoxSelected = false;
    private static boolean ignoreRWECheckBoxSelected = false;
    private static boolean ignoreLoadCheckBoxSelected = false;
    private static boolean ignoreKernelCheckBoxSelected = false;
    private static boolean ignoreDestinationCheckBoxSelected = false;
    private static boolean ignoreFinalDestinationCheckBoxSelected = false;
    private static boolean ignoreTrainCheckBoxSelected = false;

    public void initComponents(JTable carsTable) {
        _carsTable = carsTable;
        _sorter = (TableSorter) carsTable.getModel();
        _carsTableModel = (CarsTableModel) _sorter.getTableModel();

        super.initComponents();

        setTitle(Bundle.getMessage("TitleSetCars"));
        addHelpMenu("package.jmri.jmrit.operations.Operations_SetCars", true); // NOI18N
        // modify Save button text to "Change"
        saveButton.setText(Bundle.getMessage("Change"));
        // disable edit load button if no cars selected
        editLoadButton.setEnabled(false);
        // show ignore checkboxes
        ignoreStatusCheckBox.setVisible(true);
        ignoreLocationCheckBox.setVisible(true);
        ignoreRWECheckBox.setVisible(true);
        ignoreLoadCheckBox.setVisible(true);
        ignoreKernelCheckBox.setVisible(true);
        ignoreDestinationCheckBox.setVisible(true);
        ignoreFinalDestinationCheckBox.setVisible(true);
        ignoreTrainCheckBox.setVisible(true);
        ignoreAllButton.setVisible(true);

        // set the last state
        ignoreStatusCheckBox.setSelected(ignoreStatusCheckBoxSelected);
        ignoreLocationCheckBox.setSelected(ignoreLocationCheckBoxSelected);
        ignoreRWECheckBox.setSelected(ignoreRWECheckBoxSelected);
        ignoreLoadCheckBox.setSelected(ignoreLoadCheckBoxSelected);
        ignoreKernelCheckBox.setSelected(ignoreKernelCheckBoxSelected);
        ignoreDestinationCheckBox.setSelected(ignoreDestinationCheckBoxSelected);
        ignoreFinalDestinationCheckBox.setSelected(ignoreFinalDestinationCheckBoxSelected);
        ignoreTrainCheckBox.setSelected(ignoreTrainCheckBoxSelected);

        // first car in the list becomes the master
        int rows[] = _carsTable.getSelectedRows();
        if (rows.length > 0) {
            Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(rows[0]));
            super.loadCar(car);
        }
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == ignoreAllButton) {
            ignoreAll(toggle);
        }
    }

    boolean toggle = true;

    protected void ignoreAll(boolean b) {
        ignoreStatusCheckBox.setSelected(!locationUnknownCheckBox.isSelected() & b);
        ignoreLocationCheckBox.setSelected(b);
        ignoreRWECheckBox.setSelected(b);
        ignoreLoadCheckBox.setSelected(b);
        ignoreKernelCheckBox.setSelected(b);
        ignoreDestinationCheckBox.setSelected(b);
        ignoreFinalDestinationCheckBox.setSelected(b);
        ignoreTrainCheckBox.setSelected(b);
        enableComponents(!locationUnknownCheckBox.isSelected());
        toggle = !b;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    protected boolean save() {
        // save ignore states
        ignoreStatusCheckBoxSelected = ignoreStatusCheckBox.isSelected();
        ignoreLocationCheckBoxSelected = ignoreLocationCheckBox.isSelected();
        ignoreRWECheckBoxSelected = ignoreRWECheckBox.isSelected();
        ignoreLoadCheckBoxSelected = ignoreLoadCheckBox.isSelected();
        ignoreKernelCheckBoxSelected = ignoreKernelCheckBox.isSelected();
        ignoreDestinationCheckBoxSelected = ignoreKernelCheckBox.isSelected();
        ignoreFinalDestinationCheckBoxSelected = ignoreFinalDestinationCheckBox.isSelected();
        ignoreTrainCheckBoxSelected = ignoreTrainCheckBox.isSelected();

        // need to get selected cars before they are modified their location in the table can change
        List<Car> cars = new ArrayList<Car>();
        int rows[] = _carsTable.getSelectedRows();
        for (int row : rows) {
            Car car = _carsTableModel.getCarAtIndex(_sorter.modelIndex(row));
            log.debug("Adding selected car {} to change list", car.toString());
            cars.add(car);
        }
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectCars"), Bundle
                    .getMessage("carNoneSelected"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (cars.get(0) != _car) {
            log.debug("Default car isn't the first one selected");
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
                    .getMessage("doYouWantToChange"), new Object[]{cars.get(0).toString()}), Bundle
                    .getMessage("changeDefaultCar"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                super.loadCar(cars.get(0)); // new default car
                return false; // done, don't modify any of the cars selected
            }
        }

        askKernelChange = true;

        for (Car car : cars) {
            if (!super.change(car)) {
                return false;
            } else if (car.getKernel() != null && !ignoreKernelCheckBox.isSelected()) {
                askKernelChange = false;
            }
        }
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(CarsSetFrame.class.getName());
}
