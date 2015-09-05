package jd.gui.swing.jdgui.views.settings.components.jfx;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.appwork.app.gui.copycutpaste.ContextMenuAdapter;
import org.appwork.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class FXTextField extends TextField implements CaretListener, FocusListener, ContextMenuAdapter {
    /**
     * 
     */
    private static final long serialVersionUID = -3625278218179478516L;
    protected javafx.scene.paint.Color           defaultColor;
    protected javafx.scene.paint.Color           helpColor;

    {

        this.defaultColor = Color.BLACK;
        this.helpColor = (Color) UIManager.get("TextField.disabledForeground");
        if (this.helpColor == null) {
            this.helpColor = Color.LIGHTGRAY;
        }

    }
    protected String          helpText             = null;
    private boolean           setting;
    private boolean           clearHelpTextOnFocus = true;
    private boolean           helperEnabled        = true;

    public void caretUpdate(final CaretEvent arg0) {

    }

    public void focusGained(final FocusEvent arg0) {
        if (!this.isHelperEnabled()) { return; }
        if (super.getText().equals(this.helpText)) {
            if (this.isClearHelpTextOnFocus()) {
                this.setText("");
            } else {
                this.selectAll();
            }

        }
        this.setStyle("-fx-text-fill: "+ this.defaultColor);

    }

    public void focusLost(final FocusEvent arg0) {
        if (!this.isHelperEnabled()) { return; }
        if (super.getText().equals(this.helpText)) {
            this.setText(this.helpText);
            this.setStyle("-fx-text-fill: " + this.helpColor);
        }
    }

    public String getHelpText() {
        return this.helpText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.app.gui.copycutpaste.ContextMenuAdapter#getPopupMenu(org.
     * appwork.app.gui.copycutpaste.CutAction,
     * org.appwork.app.gui.copycutpaste.CopyAction,
     * org.appwork.app.gui.copycutpaste.PasteAction,
     * org.appwork.app.gui.copycutpaste.DeleteAction,
     * org.appwork.app.gui.copycutpaste.SelectAction)
     */
    @Override
    public JPopupMenu getPopupMenu(final AbstractAction cutAction, final AbstractAction copyAction, final AbstractAction pasteAction, final AbstractAction deleteAction, final AbstractAction selectAction) {
        final JPopupMenu menu = new JPopupMenu();

        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(deleteAction);
        menu.add(selectAction);
        return menu;
    }

    @Override
    public void replaceSelection(final String content) {
        if (this.isHelperEnabled() && super.getText().equals(this.helpText) && StringUtils.isNotEmpty(content)) {
            super.setText("");
        }
        super.replaceSelection(content);
        this.setStyle("-fx-text-fill: " + this.defaultColor);
    }


    public boolean isClearHelpTextOnFocus() {
        return this.clearHelpTextOnFocus;
    }

    public boolean isHelperEnabled() {
        return this.helperEnabled;
    }

    public void setHelpColor(final Color helpColor) {
        this.helpColor = helpColor;
    }

    public void setHelperEnabled(final boolean helperEnabled) {
        this.helperEnabled = helperEnabled;
    }


    public void setHelpText(final String helpText) {
        final String old = this.helpText;
        this.helpText = helpText;
        if (this.getText().length() == 0 || this.getText().equals(old)) {
            this.setText(this.helpText);
            this.setStyle("-fx-text-fill: " + this.helpColor);
        }
    }

    public void setLabelMode(final boolean b) {
        this.setEditable(!b);
        this.setFocused(!b);
        this.setBorder(b ? null : new TextArea().getBorder());
//        SwingUtils.setOpaque(this, !b);
    }
}
