package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.web.WebView;
import misc.ReloadTask;
import misc.Settings;
import misc.SettingsLoadOnStart;
import misc.Version;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;
import timeline.parser.MastodonParser;

import java.util.List;


public class Controller implements Initializable {

    MastodonAPI postMastodonAPI;
    private ReloadTask reloadTask;

    @FXML
    private TimelineViewController timelineViewController;
    @FXML
    private NotificationViewController notificationViewController;
    @FXML
    private TextArea textArea;

    @FXML private WebView webView;

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        reloadTask.manualReload();
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
        reloadTask.stop();
    }

    @FXML
    protected void onMenuItemReloadPeriod1Min(ActionEvent evt) {
        reloadTask.start();
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
        notificationViewController.registerParentControllerObject(settings, new NotificationGenerator(new MastodonParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)));
        timelineViewController.registerWebViewOutput(webView);


        this.reloadTask = new ReloadTask(List.of(timelineViewController, notificationViewController));
        this.reloadTask.manualReload();
    }

}
