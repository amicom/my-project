package org.jdownloader.gui.jdtrayicon.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JComponent;

import jd.gui.swing.jdgui.menu.ParallelDownloadsPerHostEditor;

import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuLink;
import org.jdownloader.extensions.ExtensionNotLoadedException;
import org.jdownloader.gui.translate._GUI;

public class ParallelDownloadsPerHostEditorLink extends MenuItemData implements MenuLink {

    public ParallelDownloadsPerHostEditorLink() {
        super();
        setName(_GUI._.ParalellDownloadsEditor_ParallelDownloadsPerHostEditor_());
        setIconKey("batch");
        //
    }

    @Override
    public List<AppAction> createActionsToLink() {
        return null;
    }

    @Override
    public JComponent createSettingsPanel() {
        return null;
    }

    public JComponent createItem() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, ExtensionNotLoadedException {

        return new ParallelDownloadsPerHostEditor();

    }

}
