package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import misc.Akan;
import misc.Version;


public class Controller implements Initializable {

    @FXML
    private TimelineViewController timelineViewController;
    @FXML
    private TextArea textArea;

    @FXML private WebView webView;

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        timelineViewController.viewRefresh();
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
        timelineViewController.reloadTaskStop();
    }

    @FXML
    protected void onMenuItemReloadPeriod1Min(ActionEvent evt) {
        timelineViewController.reloadTaskStart();
    }

    @FXML
    protected void onButtonInputTextPost(ActionEvent evt) {
        String text = textArea.getText();
        MastodonAPI mastodonAPI = new MastodonAPI(Akan.MASTODON_HOST, Akan.TOKEN);
        if(!text.isEmpty()) {
            mastodonAPI.postStatus(text);
            textArea.setText(""); // TODO: 成功時にクリア
        }
    }

    // TODO: image viewでuser icon
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        timelineViewController.viewRefresh();
        timelineViewController.registerWebViewOutput(webView);
    }

}
