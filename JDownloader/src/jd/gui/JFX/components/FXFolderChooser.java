package jd.gui.JFX.components;

import jd.controlling.downloadcontroller.BadDestinationException;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.PathTooLongException;
import jd.gui.swing.jdgui.views.settings.components.SettingsComponent;
import jd.gui.swing.jdgui.views.settings.components.StateUpdateEventSender;
import jd.gui.swing.jdgui.views.settings.components.StateUpdateListener;
import jd.gui.swing.jdgui.views.settings.panels.packagizer.VariableAction;
import org.appwork.swing.components.ExtTextField;
import org.appwork.uio.UIOManager;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.controlling.FileCreationManager;
import org.jdownloader.controlling.packagizer.PackagizerController;
import org.jdownloader.gui.packagehistorycontroller.DownloadPathHistoryManager;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.DownloadFolderChooserDialog;
import org.jdownloader.translate._JDT;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.io.File;

public class FXFolderChooser extends FXPathChooser implements SettingsComponent {
    /**
     *
     */
    private static final long                     serialVersionUID = 1L;

    private StateUpdateEventSender<FXFolderChooser> eventSender;

    private String                                originalPath;

    public FXFolderChooser() {
        super("FolderChooser", true);

        eventSender = new StateUpdateEventSender<FXFolderChooser>();

        this.destination.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                destination.setList(DownloadPathHistoryManager.getInstance().listPaths());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
        // CFG_LINKGRABBER.DOWNLOAD_DESTINATION_HISTORY.getEventSender().addListener(, true);
    }

    @Override
    public JPopupMenu getPopupMenu(ExtTextField txt, AbstractAction cutAction, AbstractAction copyAction, AbstractAction pasteAction, AbstractAction deleteAction, AbstractAction selectAction) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new VariableAction(txt, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_date(), "<jd:" + PackagizerController.SIMPLEDATE + ":dd.MM.yyyy>"));
        menu.add(new VariableAction(txt, _GUI._.PackagizerFilterRuleDialog_createVariablesMenu_packagename(), "<jd:" + PackagizerController.PACKAGENAME + ">"));

        return menu;
    }

    public String getConstraints() {
        return null;
    }

    public void addStateUpdateListener(StateUpdateListener listener) {
        eventSender.addListener(listener);
    }

    public void setPath(final String downloadDestination) {
        originalPath = downloadDestination;
        super.setPath(downloadDestination);
    }

    public void setText(String t) {
        setPath(t);
    }

    @Override
    public File doFileChooser() {
        File ret;
        try {
            ret = DownloadFolderChooserDialog.open(new File(txt.getText()), true, _JDT._.gui_setting_folderchooser_title());
            return ret;
        } catch (DialogClosedException e) {
            e.printStackTrace();
        } catch (DialogCanceledException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isMultiline() {
        return false;
    }

    public String getText() {
        File file = getFile();
        if (file == null) {
            return null;
        }
        file = checkPath(file, originalPath == null ? null : new File(originalPath));
        if (file == null) {
            return null;
        }
        DownloadPathHistoryManager.getInstance().add(file.getAbsolutePath());

        return file.getAbsolutePath();
    }

    public static File checkPath(File file, File presetPath) {
        String path = file.getAbsolutePath();
        File checkPath = file;
        int index = path.indexOf("<jd:");
        if (index >= 0) {
            path = path.substring(0, index);
            checkPath = new File(path);
        }
        File forbidden = null;

        try {
            DownloadWatchDog.getInstance().validateDestination(checkPath);

        } catch (PathTooLongException e) {
            forbidden = e.getFile();
        } catch (BadDestinationException e) {
            forbidden = e.getFile();
        }

        if (forbidden != null) {
            UIOManager.I().showErrorMessage(_GUI._.DownloadFolderChooserDialog_handleNonExistingFolders_couldnotcreatefolder(forbidden.getAbsolutePath()));
            return null;
        }

        if (!checkPath.exists()) {
            if (presetPath != null && presetPath.equals(checkPath)) {
                //
                return file;
            }
            final File createFolder;
            if (index >= 0) {
                createFolder = checkPath;
            } else {
                createFolder = file;
            }
            if (!createFolder.exists()) {

                if (UIOManager.I().showConfirmDialog(UIOManager.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, _GUI._.DownloadFolderChooserDialog_handleNonExistingFolders_title_(), _GUI._.DownloadFolderChooserDialog_handleNonExistingFolders_msg_(createFolder.getAbsolutePath()))) {
                    if (!FileCreationManager.getInstance().mkdir(createFolder)) {
                        UIOManager.I().showErrorMessage(_GUI._.DownloadFolderChooserDialog_handleNonExistingFolders_couldnotcreatefolder(createFolder.getAbsolutePath()));
                        return presetPath;
                    }
                }

            }
        }
        return file;
    }
}
