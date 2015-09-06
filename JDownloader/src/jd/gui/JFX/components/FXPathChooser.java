package jd.gui.JFX.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;

import java.io.File;

//TODO: suppose to replace the PathChooser class

public class FXPathChooser extends HBox {


    protected TextField txt;
    protected Button bt;
    protected ComboBox<String> destination;
    private String id;
    private SelectionMode selectionMode;
    private boolean useAsList;

    public FXPathChooser(final String id) {
        this(id, false);
    }

    public FXPathChooser(final String id, final boolean useAsList) {
        this.useAsList = useAsList;
        this.id = id;
        bt = new Button(getBrowseLabel());
        bt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final File file = doFileChooser();
                if (file != null) {
                    setFile(file);
                    updatePath(file);
                }
            }
        });

        if (useAsList) {

            destination = new ComboBox<String>();
            destination.setEditable(true);
            destination.setPromptText(getHelpText());
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
            txt = new TextField();
            txt.setPromptText(getHelpText());
            getChildren().add(txt);
            HBox.setHgrow(txt, Priority.ALWAYS);
            txt.setMaxWidth(Double.MAX_VALUE);
        }
        getChildren().add(bt);
        bt.setMinWidth(Region.USE_PREF_SIZE);
    }

    private void updatePath(File file) {
        if (useAsList)
            destination.setValue(fileToText(file));
        else
            txt.setText(fileToText(file));
    }

    public File doFileChooser() {
        File file = null;
        Stage stage = new Stage();
        switch (getSelectionMode()) {
            case DIRECTORIES_ONLY:
                DirectoryChooser folderChooser = new DirectoryChooser();
                folderChooser.setTitle(getDialogTitle());
                file = folderChooser.showDialog(stage);
                break;
            case FILES_ONLY:
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(getDialogTitle());
                fileChooser.setInitialDirectory(getFile());
                file = getType() == FileChooserType.OPEN_DIALOG ? fileChooser.showOpenDialog(stage) :
                        fileChooser.showSaveDialog(stage);
        }

        return file;

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

    public SelectionMode getSelectionMode() {
        selectionMode = SelectionMode.DIRECTORIES_ONLY;
        return selectionMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
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

    public enum SelectionMode {
        FILES_ONLY, DIRECTORIES_ONLY, FILES_AND_DIRECTORIES
    }

    public enum FileChooserType {
        OPEN_DIALOG, SAVE_DIALOG
    }
}
