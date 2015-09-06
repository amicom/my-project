package jd.gui.swing.jdgui.views.settings.panels;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jdownloader.images.NewTheme;
import org.jdownloader.translate._JDT;

/**
 * Sample Skeleton for 'GeneralSettings.fxml' Controller Class
 */


public class GeneralSettingsController {

    public Label layer;
    public ImageView image;

    public void initialize(){
        layer.setText(_JDT._.gui_settings_downloadpath_description());
        image.setImage(new Image(String.valueOf(NewTheme.I().getURL("images/", "downloadpath", ".png"))));
    }
}
