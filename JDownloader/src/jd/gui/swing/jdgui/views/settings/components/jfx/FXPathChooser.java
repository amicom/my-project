package jd.gui.swing.jdgui.views.settings.components.jfx;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.*;

import javax.swing.filechooser.FileFilter;
import java.io.File;

//TODO: suppose to replace the PathChooser class

public class FXPathChooser extends HBox {


    protected TextField txt;
    protected Button bt;
    protected ComboBox<String> destination;
    private String id;

    public FXPathChooser(final String id) {
        this(id, false);
    }

    public FXPathChooser(final String id, final boolean useQuickLIst) {

        this.id = id;
        txt = new TextField();
        txt.setPromptText(getHelpText());

        bt = new Button(getBrowseLabel());

        if (useQuickLIst) {

            destination = new ComboBox<String>();
            destination.setEditable(true);
            destination.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent event) {
                    destination.requestFocus(); // you can delete this
                    //TODO: should check if the folder in the path is valid when press ENTER. if so, lose focus. if not, present warning and discard changes to the string.
                }
            });

            getChildren().add(destination);
            HBox.setHgrow(destination, Priority.ALWAYS);
            destination.setMaxWidth(Double.MAX_VALUE);
        } else {
            getChildren().add(txt);
            HBox.setHgrow(txt, Priority.ALWAYS);
            txt.setMaxWidth(Double.MAX_VALUE);
        }
        getChildren().add(bt);
        bt.setMinWidth(Region.USE_PREF_SIZE);

        final String preSelection = JSonStorage.getStorage(Dialog.FILECHOOSER).get(Dialog.LASTSELECTION + id, this.getDefaultPreSelection());
        if (preSelection != null) {
            this.setFile(new File(preSelection));
        }
    }

    public File doFileChooser() {

        final ExtFileChooserDialog d = new ExtFileChooserDialog(0, this.getDialogTitle(), null, null);
        d.setStorageID(this.getID());
        d.setFileSelectionMode(this.getSelectionMode());
        d.setFileFilter(this.getFileFilter());
        d.setType(this.getType());
        d.setMultiSelection(false);
        d.setPreSelection(this.getFile());
        try {
            Dialog.I().showDialog(d);
        } catch (final DialogClosedException e) {
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            e.printStackTrace();
        }
        return d.getSelectedFile();

    }

    private boolean equals(final String name, final String findName) {
        if (CrossSystem.isWindows()) {
            return name.equalsIgnoreCase(findName);
        }

        return name.equals(findName);
    }

    protected String fileToText(final File file2) {
        return file2.getAbsolutePath();
    }

    public String getBrowseLabel() {
        return _AWU.T.pathchooser_browselabel();
    }

    public Button getButton() {
        return this.bt;
    }

    protected String getDefaultPreSelection() {
        return null;
    }

    public ComboBox<String> getDestination() {
        return this.destination;
    }

    public String getDialogTitle() {
        return _AWU.T.pathchooser_dialog_title();
    }

    public File getFile() {
        if (StringUtils.isEmpty(this.txt.getText())) {
            return null;
        }
        return new File(this.txt.getText());
    }

    public void setFile(final File file) {
        final String text = fileToText(file);
        if (this.destination != null) {
            this.destination.setValue(text);
        } else {
            this.txt.setText(text);
        }
    }

    public FileFilter getFileFilter() {
        return null;
    }

    protected String getHelpText() {
        return _AWU.T.pathchooser_helptext();
    }

    public void setHelpText(final String helpText) {
        txt.setPromptText(helpText);
        if (this.destination != null) {
            this.destination.setPromptText(helpText);
        }
    }

    public String getID() {
        return this.id;
    }

    public void setPath(final String downloadDestination) {

        if (this.destination != null) {
            this.destination.setValue(downloadDestination);
        } else {
            this.txt.setText(downloadDestination);
        }
    }

    public FileChooserSelectionMode getSelectionMode() {

        return FileChooserSelectionMode.DIRECTORIES_ONLY;
    }

    public FileChooserType getType() {
        return FileChooserType.SAVE_DIALOG;
    }

    public void setEnabled(final boolean b) {
        this.txt.setDisable(!b);
        this.bt.setDisable(!b);
        if (this.destination != null) {
            this.destination.setDisable(!b);
        }
    }
}
