// TrainSetColorFrame.java
package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for setting up the Trains table colors in operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 17977 $
 */
public class TrainsTableSetColorFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -7242293868179247080L;

    TrainManager trainManager = TrainManager.instance();

    // labels
    // text field
    // radio buttons
    JRadioButton manualRadioButton = new JRadioButton(Bundle.getMessage("Manual"));
    JRadioButton autoRadioButton = new JRadioButton(Bundle.getMessage("Auto"));

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("Save"));

    // combo boxes
    JComboBox<Train> trainBox = TrainManager.instance().getTrainComboBox();
    JComboBox<String> colorBox = TrainManager.instance().getRowColorComboBox();

    JComboBox<String> colorBuiltBox = TrainManager.instance().getRowColorComboBox();
    JComboBox<String> colorBuildFailedBox = TrainManager.instance().getRowColorComboBox();
    JComboBox<String> colorTrainEnRouteBox = TrainManager.instance().getRowColorComboBox();
    JComboBox<String> colorTerminatedBox = TrainManager.instance().getRowColorComboBox();

    // display panels based on which option is selected
    JPanel pTrains;
    JPanel pColor;

    JPanel pColorBuilt;
    JPanel pColorBuildFailed;
    JPanel pColorTrainEnRoute;
    JPanel pColorTerminated;

    public TrainsTableSetColorFrame(Train train) {
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		// Layout the panel by rows

        // row 1
        JPanel pOption = new JPanel();
        pOption.setLayout(new GridBagLayout());
        pOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Option")));
        addItem(pOption, manualRadioButton, 0, 0);
        addItem(pOption, autoRadioButton, 1, 0);

        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(manualRadioButton);
        bGroup.add(autoRadioButton);

        manualRadioButton.setSelected(trainManager.isRowColorManual());
        autoRadioButton.setSelected(!trainManager.isRowColorManual());

        pTrains = new JPanel();
        pTrains.setLayout(new GridBagLayout());
        pTrains.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        addItem(pTrains, trainBox, 0, 0);

        trainBox.setSelectedItem(train);

        pColor = new JPanel();
        pColor.setLayout(new GridBagLayout());
        pColor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRowColor")));
        addItem(pColor, colorBox, 0, 0);

        pColorBuilt = new JPanel();
        pColorBuilt.setLayout(new GridBagLayout());
        pColorBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRowColorBuilt")));
        addItem(pColorBuilt, colorBuiltBox, 0, 0);

        colorBuiltBox.setSelectedItem(trainManager.getRowColorNameForBuilt());

        pColorBuildFailed = new JPanel();
        pColorBuildFailed.setLayout(new GridBagLayout());
        pColorBuildFailed.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRowColorBuildFailed")));
        addItem(pColorBuildFailed, colorBuildFailedBox, 0, 0);

        colorBuildFailedBox.setSelectedItem(trainManager.getRowColorNameForBuildFailed());

        pColorTrainEnRoute = new JPanel();
        pColorTrainEnRoute.setLayout(new GridBagLayout());
        pColorTrainEnRoute.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRowColorTrainEnRoute")));
        addItem(pColorTrainEnRoute, colorTrainEnRouteBox, 0, 0);

        colorTrainEnRouteBox.setSelectedItem(trainManager.getRowColorNameForTrainEnRoute());

        // row 5
        pColorTerminated = new JPanel();
        pColorTerminated.setLayout(new GridBagLayout());
        pColorTerminated.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRowColorTerminated")));
        addItem(pColorTerminated, colorTerminatedBox, 0, 0);

        colorTerminatedBox.setSelectedItem(trainManager.getRowColorNameForTerminated());

        // row 4
        JPanel pButton = new JPanel();
        pButton.add(saveButton);

        getContentPane().add(pOption);
        getContentPane().add(pTrains);
        getContentPane().add(pColor);
        getContentPane().add(pColorBuilt);
        getContentPane().add(pColorBuildFailed);
        getContentPane().add(pColorTrainEnRoute);
        getContentPane().add(pColorTerminated);
        getContentPane().add(pButton);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainsTableColors", true); // NOI18N

        pack();
        setMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight400));

        setTitle(Bundle.getMessage("MenuItemSetTrainColor"));

        // setup buttons
        addButtonAction(saveButton);
        addRadioButtonAction(manualRadioButton);
        addRadioButtonAction(autoRadioButton);

        makePanelsVisible();

        trainManager.addPropertyChangeListener(this);
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // save option manual or auto
            trainManager.setRowColorsManual(manualRadioButton.isSelected());
            if (manualRadioButton.isSelected()) {
                Train train = (Train) trainBox.getSelectedItem();
                String colorName = (String) colorBox.getSelectedItem();
                if (train != null) {
                    train.setTableRowColorName(colorName);
                }
            } else {
                trainManager.setRowColorNameForBuildFailed((String) colorBuildFailedBox.getSelectedItem());
                trainManager.setRowColorNameForBuilt((String) colorBuiltBox.getSelectedItem());
                trainManager.setRowColorNameForTrainEnRoute((String) colorTrainEnRouteBox.getSelectedItem());
                trainManager.setRowColorNameForTerminated((String) colorTerminatedBox.getSelectedItem());
            }
            // save train file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
            return;
        }
    }

    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        makePanelsVisible();
    }

    /**
     * If manual selected, show only trains and colors available. If auto, show
     * only the three automatic options; color for train built, build failed,
     * and terminated.
     */
    private void makePanelsVisible() {
        pTrains.setVisible(manualRadioButton.isSelected());
        pColor.setVisible(manualRadioButton.isSelected());
        // the inverse
        pColorBuildFailed.setVisible(!manualRadioButton.isSelected());
        pColorBuilt.setVisible(!manualRadioButton.isSelected());
        pColorTrainEnRoute.setVisible(!manualRadioButton.isSelected());
        pColorTerminated.setVisible(!manualRadioButton.isSelected());
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            trainManager.updateTrainComboBox(trainBox);
        }
    }

    public void dispose() {
        trainManager.removePropertyChangeListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TrainsTableSetColorFrame.class.getName());
}
