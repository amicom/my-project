package jd.gui.JFX.layout;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ExtendedTitledPane extends TitledPane {


    public ExtendedTitledPane(String title) {
        super(title, new VBox());
    }

    public ExtendedTitledPane(String title, String iconUrl) {
        this(title);
        setGraphic(new ImageView(iconUrl));
    }

    @Override
    protected ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public VBox getContainer() {
        return (VBox) getContent();
    }

    public void add(Node node){
        getContainer().getChildren().add(node);
    }

}
