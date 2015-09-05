package jd.gui.swing.jdgui.views.settings.components.jfx;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.appwork.storage.JSonStorage;
import org.appwork.swing.components.ExtTextField;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;

//TODO: suppose to replace the PathChooser class

public class FXPathChooser extends HBox {





    private class BrowseAction extends AbstractAction {


        private static final long serialVersionUID = -4350861121298607806L;

        BrowseAction() {

            this.putValue(Action.NAME, FXPathChooser.this.getBrowseLabel());
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final File file = FXPathChooser.this.doFileChooser();
            if (file == null) { return; }
            FXPathChooser.this.setFile(file);

        }

    }

    private static final long  serialVersionUID = -3651657642011425583L;

    protected TextField        txt;
    protected Button           bt;
    private   String           id;
    protected ComboBox<String> destination;

    public FXPathChooser(final String id) {
        this(id, false);
    }

    public FXPathChooser(final String id, final boolean useQuickLIst) {

        this.id = id;
        txt = new TextField();
        txt.setPromptText(getHelpText());

        this.bt = new Button(getBrowseLabel());

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
        } else {
            getChildren().add(txt);
        }
        getChildren().add(bt);

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d.getSelectedFile();

    }

    private boolean equals(final String name, final String findName) {
        if (CrossSystem.isWindows()) { return name.equalsIgnoreCase(findName); }

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
        // TODO Auto-generated method stub
        return null;
    }

    public ComboBox<String> getDestination() {
        return this.destination;
    }

    public String getDialogTitle() {
        // TODO Auto-generated method stub
        return _AWU.T.pathchooser_dialog_title();
    }

    public File getFile() {
        if (StringUtils.isEmpty(this.txt.getText())) { return null; }
        return this.textToFile(this.txt.getText());
    }

    public FileFilter getFileFilter() {
        return null;
    }

    protected String getHelpText() {
        return _AWU.T.pathchooser_helptext();
    }

    public String getID() {
        return this.id;
    }

    public String getPath() {
        return new File(this.txt.getText()).getAbsolutePath();
    }

    public FileChooserSelectionMode getSelectionMode() {

        return FileChooserSelectionMode.DIRECTORIES_ONLY;
    }

    public TextField getTxt() {
        return this.txt;
    }

    public FileChooserType getType() {
        return FileChooserType.SAVE_DIALOG;
    }

    protected void onChanged(final ExtTextField txt2) {
        // TODO Auto-generated method stub
    }

    public void setEnabled(final boolean b) {
        this.txt.setDisable(!b);
        this.bt.setDisable(!b);
        if (this.destination != null) {
            this.destination.setDisable(!b);
        }
    }

    public void setFile(final File file) {
        final String text = fileToText(file);
        if (this.destination != null) {
            this.destination.setValue(text);
        } else {
            this.txt.setText(text);
        }
    }

    public void setHelpText(final String helpText) {
        txt.setPromptText(helpText);
        if (this.destination != null) {
            this.destination.setPromptText(helpText);
        }
    }

    public void setPath(final String downloadDestination) {

        if (this.destination != null) {
            this.destination.setValue(downloadDestination);
        } else {
            this.txt.setText(downloadDestination);
        }

    }
    protected File textToFile(final String text) {
        return new File(text);
    }

}
