package jmri.jmrit.display;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionableLabel is a JLabel that can be dragged around the inside of the
 * enclosing Container using a right-drag.
 * <P>
 * The positionable parameter is a global, set from outside. The 'fixed'
 * parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision$
 */
public class PositionableLabel extends JLabel implements Positionable {

    private static final long serialVersionUID = 2620446240151660560L;

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    protected Editor _editor;

    private boolean debug = false;
    protected boolean _icon = false;
    protected boolean _text = false;
    protected boolean _control = false;
    protected NamedIcon _namedIcon;

    protected ToolTip _tooltip;
    protected boolean _showTooltip = true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = true;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int _displayLevel;

    protected String _unRotatedText;
    protected boolean _rotateText = false;
    private int _degrees;

    public PositionableLabel(String s, Editor editor) {
        super(s);
        _editor = editor;
        _text = true;
        _unRotatedText = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (text) " + s);
        }
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public PositionableLabel(NamedIcon s, Editor editor) {
        super(s);
        _editor = editor;
        _icon = true;
        _namedIcon = s;
        debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PositionableLabel ctor (icon) " + s.getName());
        }
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public final boolean isIcon() {
        return _icon;
    }

    public final boolean isText() {
        return _text;
    }

    public final boolean isControl() {
        return _control;
    }

    public Editor getEditor() {
        return _editor;
    }

    public void setEditor(Editor ed) {
        _editor = ed;
    }

    /**
     * *************** Positionable methods *********************
     */
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    public final boolean isPositionable() {
        return _positionable;
    }

    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
    }

    public boolean isEditable() {
        return _editable;
    }

    public void setViewCoordinates(boolean enabled) {
        _viewCoordinates = enabled;
    }

    public boolean getViewCoordinates() {
        return _viewCoordinates;
    }

    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    public boolean isControlling() {
        return _controlling;
    }

    public void setHidden(boolean hide) {
        _hidden = hide;
        showHidden();
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void showHidden() {
        if (!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    /**
     * Delayed setDisplayLevel for DnD
     */
    public void setLevel(int l) {
        _displayLevel = l;
    }

    public void setDisplayLevel(int l) {
        int oldDisplayLevel = _displayLevel;
        _displayLevel = l;
        if (oldDisplayLevel != l) {
            if (debug) {
                log.debug("Changing label display level from " + oldDisplayLevel + " to " + _displayLevel);
            }
            _editor.displayLevelChange(this);
        }
    }

    public int getDisplayLevel() {
        return _displayLevel;
    }

    public void setShowTooltip(boolean set) {
        _showTooltip = set;
    }

    public boolean showTooltip() {
        return _showTooltip;
    }

    public void setTooltip(ToolTip tip) {
        _tooltip = tip;
    }

    public ToolTip getTooltip() {
        return _tooltip;
    }

    public String getNameString() {
        if (_icon && _displayLevel > Editor.BKG) {
            return "Icon";
        } else if (_text) {
            return "Text Label";
        } else {
            return "Background";
        }
    }

    public Positionable deepClone() {
        PositionableLabel pos;
        if (_icon) {
            NamedIcon icon = new NamedIcon((NamedIcon) getIcon());
            pos = new PositionableLabel(icon, _editor);
        } else {
            pos = new PositionableLabel(getText(), _editor);
        }
        return finishClone(pos);
    }

    /**
     * When text is rotated or in an icon mode, the return of getText() may be
     * null or some other value
     *
     * @return original defining text set by user
     */
    public String getUnRotatedText() {
        return _unRotatedText;
    }
    public void setUnRotatedText(String s) {
        _unRotatedText = s;
    }

    public Positionable finishClone(Positionable p) {
        PositionableLabel pos = (PositionableLabel) p;
        pos._text = _text;
        pos._icon = _icon;
        pos._control = _control;
//        pos._rotateText = _rotateText;
        pos._unRotatedText = _unRotatedText;
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip = _showTooltip;
        pos.setTooltip(getTooltip());
        pos._editable = _editable;
        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone(pos, pos.getTextComponent()));
        }
        pos.setOpaque(isOpaque());
        if (_namedIcon != null) {
            pos._namedIcon = cloneIcon(_namedIcon, pos);
            pos.setIcon(pos._namedIcon);
            pos.rotate(_degrees);		//this will change text in icon with a new _namedIcon.
        }
        pos.updateSize();
        return pos;
    }

    public JComponent getTextComponent() {
        return this;
    }

    public static NamedIcon cloneIcon(NamedIcon icon, PositionableLabel pos) {
        if (icon.getURL() != null) {
            return new NamedIcon(icon, pos);
        } else {
            NamedIcon clone = new NamedIcon(icon.getImage());
            clone.scale(icon.getScale(), pos);
            clone.rotate(icon.getDegrees(), pos);
            return clone;
        }
    }

    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {
    }

    public void doMouseReleased(MouseEvent event) {
    }

    public void doMouseClicked(MouseEvent event) {
    }

    public void doMouseDragged(MouseEvent event) {
    }

    public void doMouseMoved(MouseEvent event) {
    }

    public void doMouseEntered(MouseEvent event) {
    }

    public void doMouseExited(MouseEvent event) {
    }

    public boolean storeItem() {
        return true;
    }

    public boolean doViemMenu() {
        return true;
    }

    /**
     * ************** end Positionable methods *********************
     */
    /**
     * *************************************************************
     */
    PositionablePopupUtil _popupUtil;

    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }

    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed is
     * changed
     */
    public void updateSize() {
        if (debug) {
            log.trace("updateSize() w= " + maxWidth() + ", h= " + maxHeight() + " _namedIcon= " + _namedIcon);
        }

        setSize(maxWidth(), maxHeight());
        if (_namedIcon != null && _text) {
            //we have a combined icon/text therefore the icon is central to the text.
            setHorizontalTextPosition(CENTER);
        }
    }

    public int maxWidth() {
        if (_rotateText && _namedIcon != null) {
            return _namedIcon.getIconWidth();
        }
        if (_popupUtil == null) {
            return maxWidthTrue();
        }

        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                return maxHeightTrue();
            default:
                return maxWidthTrue();
        }
    }

    public int maxHeight() {
        if (_rotateText && _namedIcon != null) {
            return _namedIcon.getIconHeight();
        }
        if (_popupUtil == null) {
            return maxHeightTrue();
        }
        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                return maxWidthTrue();
            default:
                return maxHeightTrue();
        }
    }

    public int maxWidthTrue() {
        int max = 0;
        if (_popupUtil != null && _popupUtil.getFixedWidth() != 0) {
            max = _popupUtil.getFixedWidth();
            max += _popupUtil.getBorderSize() * 2;
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                max = PositionablePopupUtil.MIN_SIZE;
            }
        } else {
            if (_text && getText() != null) {
                if (getText().trim().length() == 0) {
                    // show width of 1 blank character
                    if (getFont() != null) {
                        max = getFontMetrics(getFont()).stringWidth("0");
                    }
                } else {
                    max = getFontMetrics(getFont()).stringWidth(getText());
                }
            }
            if (_icon && _namedIcon != null) {
                max = Math.max(_namedIcon.getIconWidth(), max);
            }
            if (_text && _popupUtil != null) {
                max += _popupUtil.getMargin() * 2;
                max += _popupUtil.getBorderSize() * 2;
            }
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                max = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (debug) {
            log.trace("maxWidth= " + max + " preferred width= " + getPreferredSize().width);
        }
        return max;
    }

    public int maxHeightTrue() {
        int max = 0;
        if (_popupUtil != null && _popupUtil.getFixedHeight() != 0) {
            max = _popupUtil.getFixedHeight();
            max += _popupUtil.getBorderSize() * 2;
            if (max < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
            }
        } else {
            //if(_text) {
            if (_text && getText() != null && getFont() != null) {
                max = getFontMetrics(getFont()).getHeight();
            }
            if (_icon && _namedIcon != null) {
                max = Math.max(_namedIcon.getIconHeight(), max);
            }
            if (_text && _popupUtil != null) {
                max += _popupUtil.getMargin() * 2;
                max += _popupUtil.getBorderSize() * 2;
            }
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                max = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (debug) {
            log.trace("maxHeight= " + max + " preferred height= " + getPreferredSize().height);
        }
        return max;
    }

    public boolean isBackground() {
        return (_displayLevel == Editor.BKG);
    }

    public boolean isRotated() {
        return _rotateText;
    }

    public void updateIcon(NamedIcon s) {
        _namedIcon = s;
        super.setIcon(_namedIcon);
        updateSize();
    }

    /**
     * ***** Methods to add menu items to popup *******
     */
    /**
     * Call to a Positionable that has unique requirements - e.g.
     * RpsPositionIcon, SecurityElementIcon
     */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
     * Rotate othogonally return true if popup is set
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {

        if (isIcon() && _displayLevel > Editor.BKG) {
            popup.add(new AbstractAction(Bundle.getMessage("Rotate")) {
                /**
                 *
                 */
                private static final long serialVersionUID = -3965855672806759644L;

                public void actionPerformed(ActionEvent e) {
                    rotateOrthogonal();
                }
            });
            return true;
        }
        return false;
    }

    protected void rotateOrthogonal() {
        _namedIcon.setRotation(_namedIcon.getRotation() + 1, this);
        super.setIcon(_namedIcon);
        updateSize();
        repaint();
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    /**
     * ********** Methods for Item Popups in Panel editor
     * ************************
     */
    JFrame _iconEditorFrame;
    IconAdder _iconEditor;

    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
            popup.add(new AbstractAction(txt) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1481028540455022L;

                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * For item popups in Panel Editor
     */
    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor != null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                _iconEditorFrame.dispose();
                _iconEditorFrame = null;
            }
        });
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
    }

    protected void edit() {
        makeIconEditorFrame(this, "Icon", false, null);
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editIcon();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);

    }

    protected void editIcon() {
        String url = _iconEditor.getIcon("plainIcon").getURL();
        _namedIcon = NamedIcon.getIconByName(url);
        super.setIcon(_namedIcon);
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    public jmri.util.JmriJFrame _paletteFrame;

    /**
     * ********** Methods for Item Popups in Control Panel editor
     * *******************
     */
    /**
     * For item popups in Control Panel Editor
     */
    protected void makePalettteFrame(String title) {
        jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);

        _paletteFrame = new jmri.util.JmriJFrame(title, false, false);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                ImageIndexEditor.checkImageIndex();   // write maps to tree
            }
        });
    }

    /**
     * Rotate degrees return true if popup is set
     */
    public boolean setRotateMenu(JPopupMenu popup) {
        if (_displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getRotateEditAction(this));
            return true;
        }
        return false;
    }

    /**
     * Scale percentage return true if popup is set
     */
    public boolean setScaleMenu(JPopupMenu popup) {
        if (isIcon() && _displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getScaleEditAction(this));
            return true;
        }
        return false;
    }

    public boolean setTextEditMenu(JPopupMenu popup) {
        if (isText()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "EditText"));
            return true;
        }
        return false;
    }

    JCheckBoxMenuItem disableItem = null;

    public boolean setDisableControlMenu(JPopupMenu popup) {
        if (_control) {
            disableItem = new JCheckBoxMenuItem(Bundle.getMessage("Disable"));
            disableItem.setSelected(!_controlling);
            popup.add(disableItem);
            disableItem.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setControlling(!disableItem.isSelected());
                }
            });
            return true;
        }
        return false;
    }

    public void setScale(double s) {
        if (_namedIcon != null) {
            _namedIcon.scale(s, this);
            super.setIcon(_namedIcon);
            updateSize();
        }
    }

    public double getScale() {
        if (_namedIcon == null) {
            return 1.0;
        }
        return ((NamedIcon) getIcon()).getScale();
    }
    
    public void setIcon(NamedIcon icon) {
        _namedIcon = icon;
        super.setIcon(icon);
    }
    
    public void rotate(int deg) {
        log.debug("rotate({}) with _rotateText {}, _text {}, _icon {}", deg, _rotateText, _text, _icon);
        _degrees = deg;
        if (_rotateText || deg==0) {
            if (deg == 0) {             // restore unrotated whatever
                _rotateText = false;
                if(_text) {
                    log.debug("   super.setText(\"{}\");", _unRotatedText);
                    super.setText(_unRotatedText);
                    if (_popupUtil!=null) {
                        setOpaque( _popupUtil.hasBackground());
                        _popupUtil.setBorder(true);                        
                    }
                    if (_icon) {
                        String url = _namedIcon.getURL();
                        _namedIcon = new NamedIcon(url, url);                        
                    } else {
                        _namedIcon = null;
                    }
                    super.setIcon(_namedIcon);
                } else {
                    _namedIcon.rotate(deg, this);
                    super.setIcon(_namedIcon);
                }
            } else {
                if (_text & _icon) {    // update text over icon
                    _namedIcon = makeTextOverlaidIcon(_unRotatedText, _namedIcon);
                } else if (_text) {     // update text only icon image                  
                    _namedIcon = makeTextIcon(_unRotatedText);
                }
                _namedIcon.rotate(deg, this);
                super.setIcon(_namedIcon);
                setOpaque(false);   // rotations cannot be opaque
            }
        } else {
            if (deg != 0) { // first time text or icon is rotated from horizontal
                if (_text && _icon) {   // text overlays icon  e.g. LocoIcon
                    _namedIcon = makeTextOverlaidIcon(_unRotatedText, _namedIcon);
                    super.setText(null);
                    _rotateText = true;
                    setOpaque(false);
                } else if (_text) {
                    _namedIcon = makeTextIcon(_unRotatedText);
                    super.setText(null);
                    _rotateText = true;
                    setOpaque(false);
                }
                if (_popupUtil!=null) {
                    _popupUtil.setBorder(false);
                }
                _namedIcon.rotate(deg, this);
                super.setIcon(_namedIcon);
            } else if (_namedIcon != null) {
                _namedIcon.rotate(deg, this);
                super.setIcon(_namedIcon);
            }
        }
        updateSize();
    }

    /**
     * Create an image of icon with text overlaid
     */
    protected NamedIcon makeTextOverlaidIcon(String text, NamedIcon ic) {
        String url = ic.getURL();
        NamedIcon icon = new NamedIcon(url, url);
        int textWidth = getFontMetrics(getFont()).stringWidth(text);
        int iconWidth = icon.getIconWidth();
        int textHeight = getFontMetrics(getFont()).getHeight();
        int iconHeight = icon.getIconHeight();

        int width = Math.max(textWidth, iconWidth);
        int height = Math.max(textHeight, iconHeight);
        int hOffset = Math.max((textWidth - iconWidth) / 2, 0);
        int vOffset = Math.max((textHeight - iconHeight) / 2, 0);

        if (_popupUtil != null) {
            if (_popupUtil.getFixedWidth() != 0) {
                switch (_popupUtil.getJustification()) {
                    case PositionablePopupUtil.LEFT:
                        hOffset = _popupUtil.getBorderSize();
                        break;
                    case PositionablePopupUtil.RIGHT:
                        hOffset = _popupUtil.getFixedWidth() - width;
                        hOffset += _popupUtil.getBorderSize();
                        break;
                    default:
                        hOffset = Math.max((_popupUtil.getFixedWidth() - width) / 2, 0);
                        hOffset += _popupUtil.getBorderSize();
                        break;
                }
                width = _popupUtil.getFixedWidth() + 2 * _popupUtil.getBorderSize();
            } else {
                width += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                hOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
            if (_popupUtil.getFixedHeight() != 0) {
                vOffset = Math.max(vOffset + (_popupUtil.getFixedHeight() - height) / 2, 0);
                vOffset += _popupUtil.getBorderSize();
                height = _popupUtil.getFixedHeight() + 2 * _popupUtil.getBorderSize();
            } else {
                height += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                vOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
        }
        
        BufferedImage bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (_popupUtil != null) {
            if ( _popupUtil.hasBackground()) {
                g2d.setColor(_popupUtil.getBackground());
                g2d.fillRect(0, 0, width, height);
            }
            if (_popupUtil.getBorderSize() != 0) {
                g2d.setColor(_popupUtil.getBorderColor());
                g2d.setStroke(new java.awt.BasicStroke(2 * _popupUtil.getBorderSize()));
                g2d.drawRect(0, 0, width, height);
            }
        }

        g2d.drawImage(icon.getImage(), AffineTransform.getTranslateInstance(hOffset, vOffset+1), this);
        g2d.setFont(getFont());
        
        hOffset = Math.max((width - textWidth) / 2, 0);
        vOffset = Math.max((height - textHeight) / 2, 0) + getFontMetrics(getFont()).getAscent();
        g2d.setColor(getForeground());
        g2d.drawString(text, hOffset, vOffset);

        icon = new NamedIcon(bufIm);
        g2d.dispose();
        icon.setURL(url);
        return icon;
    }

    /**
     * create a text image whose bit map can be rotated
     *
     * @param text
     * @return
     */
    private NamedIcon makeTextIcon(String text) {
        if (text == null || text.equals("")) {
            text = " ";
        }
        int width = getFontMetrics(getFont()).stringWidth(text);
        int height = getFontMetrics(getFont()).getHeight();
        int hOffset = 0;
        int vOffset = getFontMetrics(getFont()).getAscent();
        if (_popupUtil != null) {
            if (_popupUtil.getFixedWidth() != 0) {
                switch (_popupUtil.getJustification()) {
                    case PositionablePopupUtil.LEFT:
                        hOffset = _popupUtil.getBorderSize();
                        break;
                    case PositionablePopupUtil.RIGHT:
                        hOffset = _popupUtil.getFixedWidth() - width;
                        hOffset += _popupUtil.getBorderSize();
                        break;
                    default:
                        hOffset = Math.max((_popupUtil.getFixedWidth() - width) / 2, 0);
                        hOffset += _popupUtil.getBorderSize();
                        break;
                }
                width = _popupUtil.getFixedWidth() + 2 * _popupUtil.getBorderSize();
            } else {
                width += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                hOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
            if (_popupUtil.getFixedHeight() != 0) {
                vOffset = Math.max(vOffset + (_popupUtil.getFixedHeight() - height) / 2, 0);
                vOffset += _popupUtil.getBorderSize();
                height = _popupUtil.getFixedHeight() + 2 * _popupUtil.getBorderSize();
            } else {
                height += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                vOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
        }
        BufferedImage bufIm = new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();
        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (_popupUtil != null) {
            if ( _popupUtil.hasBackground()) {
                g2d.setColor(_popupUtil.getBackground());
                g2d.fillRect(0, 0, width, height);
            }
            if (_popupUtil.getBorderSize() != 0) {
                g2d.setColor(_popupUtil.getBorderColor());
                g2d.setStroke(new java.awt.BasicStroke(2 * _popupUtil.getBorderSize()));
                g2d.drawRect(0, 0, width, height);
            }
        }
        g2d.setColor(getForeground());
        g2d.drawString(text, hOffset, vOffset);
        NamedIcon icon = new NamedIcon(bufIm);
        g2d.dispose();
        return icon;
    }

    public void setDegrees(int deg) {
        _degrees = deg;
    }

    public int getDegrees() {
        return _degrees;
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    public void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    public void remove() {
        _editor.removeFromContents(this);
        // remove from persistance by flagging inactive
        active = false;
        dispose();
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    protected void setSuperText(String text) {
        _unRotatedText = text;
        super.setText(text);
    }

    @Override
    public void setText(String text) {
        _unRotatedText = text;
        _text = (text !=null && text.length()>0);  // when "" is entered for text, and a font has been specified, the descender distance moves the position
        if (/*_rotateText &&*/ !isIcon() && _namedIcon != null) {
            log.debug("setText calls rotate({})", _degrees);
            rotate(_degrees);		//this will change text label as a icon with a new _namedIcon.
        } else {
            log.debug("setText calls super.setText()");
            super.setText(text);
        }
    }

    private boolean needsRotate;

    @Override
    public Dimension getSize() {
        if (!needsRotate) {
            return super.getSize();
        }

        Dimension size = super.getSize();
        if (_popupUtil == null) {
            return super.getSize();
        }
        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                return new Dimension(size.height, size.width);
            default:
                return super.getSize();
        }
    }

    @Override
    public int getHeight() {
        return getSize().height;
    }

    @Override
    public int getWidth() {
        return getSize().width;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (_popupUtil == null) {
            super.paintComponent(g);
        } else {
            Graphics2D gr = (Graphics2D) g.create();

            switch (_popupUtil.getOrientation()) {
                case PositionablePopupUtil.VERTICAL_UP:
                    gr.translate(0, getSize().getHeight());
                    gr.transform(AffineTransform.getQuadrantRotateInstance(-1));
                    break;
                case PositionablePopupUtil.VERTICAL_DOWN:
                    gr.transform(AffineTransform.getQuadrantRotateInstance(1));
                    gr.translate(0, -getSize().getWidth());
                    break;
                default:
            }

            needsRotate = true;
            super.paintComponent(gr);
            needsRotate = false;
        }
    }

    /**
     * Provides a generic method to return the bean associated with the
     * Positionable
     */
    public jmri.NamedBean getNamedBean() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableLabel.class.getName());

}
