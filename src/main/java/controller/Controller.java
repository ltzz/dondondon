package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import timeline.TimelineGenerator;
import misc.Version;


public class Controller implements Initializable {

    @FXML
    private AnchorPane tableView;
    @FXML
    private TableViewController tableViewController;

    @FXML private WebView webView;

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        tableViewController.tabRefresh();
    }

    @FXML
    protected void onMenuItemVersion(ActionEvent evt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("バージョン情報");
        alert.getDialogPane().setHeaderText("バージョン");
        alert.getDialogPane().setContentText(Version.versionString());
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
        System.out.println(button.toString());

    }

    @FXML
    protected void onMenuItemReloadPeriodNone(ActionEvent evt) {
        tableViewController.reloadTaskStop();
    }

    @FXML
    protected void onMenuItemReloadPeriod1Min(ActionEvent evt) {
        tableViewController.reloadTaskStart();
    }


    public static class TootCell extends TableRow<TimelineGenerator.TootContent> {
        @Override
        protected void updateItem(TimelineGenerator.TootContent tootContent, boolean empty){
            super.updateItem(tootContent, empty);
        }
    }

    // TODO: image viewでuser icon
    // TODO: filter ██
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        tableViewController.tabRefresh();
        tableViewController.registerWebViewOutput(webView);
    }

}
