package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import misc.Settings;
import misc.SettingsLoadOnStart;
import misc.Version;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;
import timeline.parser.MastodonParser;


public class Controller implements Initializable {

    MastodonAPI postMastodonAPI;

    @FXML
    private TimelineViewController timelineViewController;
    @FXML
    private NotificationViewController notificationViewController;
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
        if(!text.isEmpty()) {
            postMastodonAPI.postStatus(text);
            textArea.setText(""); // TODO: 成功時にクリア
        }
    }

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        Settings settings = new Settings();
        if(true) { // For Developper:　設定保存用
            SettingsLoadOnStart settingsLoadOnStart = new SettingsLoadOnStart(settings);
            settingsLoadOnStart.startSequence();
        }

        postMastodonAPI = new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken);

        timelineViewController.registerParentControllerObject(settings,
                new TimelineGenerator(new MastodonParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)),
                new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken));
        timelineViewController.viewRefresh();
        notificationViewController.registerParentControllerObject(settings, new NotificationGenerator(new MastodonParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)));
        notificationViewController.viewRefresh(); // FIXME: 起動時にしか通知を読み込んでないので、リロード時にも読むようにする
        timelineViewController.registerWebViewOutput(webView);
    }

}
