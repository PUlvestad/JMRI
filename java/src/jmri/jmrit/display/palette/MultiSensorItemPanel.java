package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import jmri.NamedBean;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiSensorItemPanel extends TableItemPanel {

    /**
     *
     */
    private static final long serialVersionUID = -743088921159606185L;
    JPanel _multiSensorPanel;
    MultiSensorSelectionModel _selectionModel;
    boolean _upDown = false;

    public MultiSensorItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
        setToolTipText(Bundle.getMessage("ToolTipDragSelection"));
    }

    @Override
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _table = model.makePickTable();
        ROW_HEIGHT = _table.getRowHeight();
        TableColumn column = new TableColumn(PickListModel.POSITION_COL);
        column.setHeaderValue("Position");
        _table.addColumn(column);
        _selectionModel = new MultiSensorSelectionModel(model);
        _table.setSelectionModel(_selectionModel);
        _table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel(model.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
        _scrollPane = new JScrollPane(_table);
        topPanel.add(_scrollPane, BorderLayout.CENTER);
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));

        JPanel panel = new JPanel();
        _addTableButton = new JButton(Bundle.getMessage("CreateNewItem"));
        _addTableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                makeAddToTableWindow();
            }
        });
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        panel.add(_addTableButton);

        int size = 6;
        if (_family != null) {
            HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
            size = map.size();
        }
        _selectionModel.setPositionRange(size - 3);
        JButton clearSelectionButton = new JButton(Bundle.getMessage("ClearSelection"));
        clearSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                clearSelections();
            }
        });
        clearSelectionButton.setToolTipText(Bundle.getMessage("ToolTipClearSelection"));
        panel.add(clearSelectionButton);
        topPanel.add(panel, BorderLayout.SOUTH);
        _table.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        _scrollPane.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        topPanel.setToolTipText(Bundle.getMessage("ToolTipDragTableRow"));
        return topPanel;
    }

    public void clearSelections() {
        _selectionModel.clearSelection();
        int size = 6;
//        if (_family!=null) {
//            HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
//            size = map.size();
//        }
        if (_currentIconMap != null) {
            size = _currentIconMap.size();
        }
        _selectionModel.setPositionRange(size - 3);
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        super.makeDndIconPanel(iconMap, "second");
    }

    @Override
    protected void initIconFamiliesPanel() {
        super.initIconFamiliesPanel();
        makeMultiSensorPanel();
        _iconFamilyPanel.add(_multiSensorPanel);
    }

    private void makeMultiSensorPanel() {
        _multiSensorPanel = new JPanel();
        _multiSensorPanel.setLayout(new BoxLayout(_multiSensorPanel, BoxLayout.Y_AXIS));
        JPanel panel2 = new JPanel();
        ButtonGroup group2 = new ButtonGroup();
        JRadioButton button = new JRadioButton(Bundle.getMessage("LeftRight"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _upDown = false;
            }
        });
        group2.add(button);
        panel2.add(button);
        button.setSelected(true);
        button = new JRadioButton(Bundle.getMessage("UpDown"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _upDown = true;
            }
        });
        group2.add(button);
        panel2.add(button);
        _multiSensorPanel.add(panel2);
        _multiSensorPanel.repaint();
    }

    @Override
    protected void setFamily(String family) {
        super.setFamily(family);
        if (_multiSensorPanel != null) {
            _iconFamilyPanel.remove(_multiSensorPanel);
        }
        makeMultiSensorPanel();
        _iconFamilyPanel.add(_multiSensorPanel);
        _iconFamilyPanel.repaint();
        updateFamiliesPanel();
        setSelections();
    }

    protected void setSelections() {
        int[] positions = _selectionModel.getPositions();
        clearSelections();
        int len = Math.min(positions.length, _selectionModel.getPositions().length);
        for (int i = 0; i < len; i++) {
            if (positions[i] > -1) {
                _selectionModel.setSelectionInterval(positions[i], positions[i]);
            }
        }
    }

    @Override
    protected IconDialog openDialog(String type, String family, HashMap<String, NamedIcon> iconMap) {
        IconDialog dialog = new MultiSensorIconDialog(type, family, this, iconMap);
        dialog.sizeLocate();
        return dialog;
    }

    /*    protected void createNewFamily(String type) {
     _newFamilyDialog = new MultiSensorIconDialog(_itemType, null, this, null);
     _newFamilyDialog.sizeLocate();
     }
     */
    /**
     * Used by Panel Editor to make updates the icon(s) into the user's Panel.
     */
    public ArrayList<NamedBean> getTableSelections() {
        return _selectionModel.getSelections();
    }

    public int[] getPositions() {
        return _selectionModel.getPositions();
    }

    public boolean getUpDown() {
        return _upDown;
    }

    @Override
    public void setSelection(NamedBean bean) {
        int row = _model.getIndexOf(bean);
        if (row >= 0) {
            _selectionModel.setSelectionInterval(row, row);
            _scrollPane.getVerticalScrollBar().setValue(row * ROW_HEIGHT);
        } else {
            valueChanged(null);
        }
    }

    public void setUpDown(boolean upDown) {
        _upDown = upDown;
    }

    static final String[] POSITION = {"first", "second", "third", "fourth", "fifth",
        "sixth", "seventh", "eighth", "nineth", "tenth"};

    static public String getPositionName(int index) {
        return POSITION[index];
    }

    protected class MultiSensorSelectionModel extends DefaultListSelectionModel {

        /**
         *
         */
        private static final long serialVersionUID = 8448986920160037659L;
        ArrayList<NamedBean> _selections;
        int[] _positions;
        int _nextPosition;
        PickListModel _tableModel;

        MultiSensorSelectionModel(PickListModel tableModel) {
            super();
            _tableModel = tableModel;
            setPositionRange(0);
        }

        protected ArrayList<NamedBean> getSelections() {
            if (log.isDebugEnabled()) {
                log.debug("getSelections: size= " + _selections.size()
                        + ", _nextPosition= " + _nextPosition);
            }
            if (_nextPosition < _positions.length) {
                return null;
            }
            return _selections;
        }

        protected int[] getPositions() {
            int[] positions = new int[_positions.length];
            System.arraycopy(_positions, 0, positions, 0, _positions.length);
            return positions;
        }

        protected int getNextPosition() {
            return _nextPosition;
        }

        protected void setPositionRange(int size) {
            if (log.isDebugEnabled()) {
                log.debug("setPositionRange: size= " + size);
            }
            if (size > POSITION.length) {
                size = POSITION.length;
            }
            _positions = new int[size];
            for (int i = 0; i < size; i++) {
                _positions[i] = -1;
            }
            _selections = new ArrayList<NamedBean>(size);
            _nextPosition = 0;
        }

        /**
         * ************* DefaultListSelectionModel overrides
         * *******************
         */
        @Override
        public boolean isSelectedIndex(int index) {
            for (int i = 0; i < _positions.length; i++) {
                if (_positions[i] == index) {
                    if (log.isDebugEnabled()) {
                        log.debug("isSelectedIndex(" + index + ") returned true");
                    }
                    return true;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("isSelectedIndex(" + index + ") returned false");
            }
            return false;
        }

        @Override
        public void clearSelection() {
            if (log.isDebugEnabled()) {
                log.debug("clearSelection()");
            }
            for (int i = 0; i < _positions.length; i++) {
                if (_positions[i] >= 0) {
                    _tableModel.setValueAt(null, _positions[i], PickListModel.POSITION_COL);
                    super.setSelectionInterval(_positions[i], _positions[i]);
                    super.clearSelection();
                    _positions[i] = -1;
                }
            }
            _selections = new ArrayList<NamedBean>(_positions.length);
            _nextPosition = 0;
        }

        @Override
        public void addSelectionInterval(int index0, int index1) {
            if (log.isDebugEnabled()) {
                log.debug("addSelectionInterval(" + index0 + ", " + index1 + ") - stubbed");
            }
//            super.addSelectionInterval(index0, index1);
        }

        @Override
        public void setSelectionInterval(int row, int index1) {
            if (_nextPosition >= _positions.length) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        Bundle.getMessage("NeedIcon", _selectionModel.getPositions().length),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("setSelectionInterval(" + row + ", " + index1 + ")");
            }
            NamedBean bean = _tableModel.getBeanAt(row);
            String position = (String) _tableModel.getValueAt(row, PickListModel.POSITION_COL);
            if (position != null && position.length() > 0) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        Bundle.getMessage("DuplicatePosition",
                                new Object[]{bean.getDisplayName(), position}),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
            } else {
                _tableModel.setValueAt(Bundle.getMessage(POSITION[_nextPosition]), row, PickListModel.POSITION_COL);
                _selections.add(_nextPosition, bean);
                _positions[_nextPosition] = row;
                _nextPosition++;
                super.setSelectionInterval(row, row);
            }
        }
    }

    @Override
    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map) {
        return new IconDragJLabel(flavor, map);
    }

    protected class IconDragJLabel extends DragJLabel {

        /**
         *
         */
        private static final long serialVersionUID = 451205074058595934L;
        HashMap<String, NamedIcon> iconMap;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP2") // icon map is within package 
        public IconDragJLabel(DataFlavor flavor, HashMap<String, NamedIcon> map) {
            super(flavor);
            iconMap = map;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }
            _selectionModel.getPositions();

            MultiSensorIcon ms = new MultiSensorIcon(_editor);
            ms.setInactiveIcon(new NamedIcon(iconMap.get("SensorStateInactive")));
            ms.setInconsistentIcon(new NamedIcon(iconMap.get("BeanStateInconsistent")));
            ms.setUnknownIcon(new NamedIcon(iconMap.get("BeanStateUnknown")));
            ArrayList<NamedBean> selections = _selectionModel.getSelections();
            if (selections == null) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        Bundle.getMessage("NeedPosition", _selectionModel.getPositions().length),
                        Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            for (int i = 0; i < selections.size(); i++) {
                ms.addEntry(selections.get(i).getDisplayName(), new NamedIcon(iconMap.get(POSITION[i])));
            }
            _selectionModel.clearSelection();
            ms.setFamily(_family);
            ms.setUpDown(_upDown);
            ms.setLevel(Editor.SENSORS);
            return ms;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MultiSensorItemPanel.class.getName());
}
