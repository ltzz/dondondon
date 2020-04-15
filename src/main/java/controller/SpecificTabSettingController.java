package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

public class SpecificTabSettingController implements Initializable {
    @FXML
    private TreeView<String> treeView;

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        TreeItem<String> rootItem = new TreeItem<>("root");
        TreeItem<String> server = new CheckBoxTreeItem<>("hogehoge.example.com");
        server.getChildren().add(new CheckBoxTreeItem<>("Home"));
        server.getChildren().add(new CheckBoxTreeItem<>("Local"));
        server.getChildren().add(new CheckBoxTreeItem<>("Notification"));
        server.setExpanded(true);
        treeView.setCellFactory((TreeView<String> p) -> new CheckBoxTreeCell<String>());
        rootItem.getChildren().add(server);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

    }
}
