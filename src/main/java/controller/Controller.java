package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.layout.AnchorPane;
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

    // @FXML
    private TimelineViewController timelineViewController;
    // @FXML
    private NotificationViewController notificationViewController;
    @FXML
    private TextArea textArea;

    @FXML
    private TabPane tabPane;

    @FXML private WebView webView;

    @FXML private CheckMenuItem userIconVisible;

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
    protected void onMenuItemUserIconInvisible(ActionEvent evt) {
        if( userIconVisible.selectedProperty().get() ){
            timelineViewController.iconInvisible(true);
            notificationViewController.iconInvisible(true);
        }
        else {
            timelineViewController.iconInvisible(false);
            notificationViewController.iconInvisible(false);
        }
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

        // 動的タブ追加のテスト
        try {
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                Tab tab = new Tab("default");
                tab.setClosable(false);
                AnchorPane pane = loader.load();
                tab.setContent(pane);
                timelineViewController = loader.getController();
                tabPane.getTabs().add(tab);
                timelineViewController.registerParentControllerObject(settings,
                        new TimelineGenerator(new MastodonParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)),
                        new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken));
                timelineViewController.registerWebViewOutput(webView);
            }
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/notification_view.fxml"));
                Tab tab = new Tab("notification");
                tab.setClosable(false);
                AnchorPane pane = loader.load();
                tab.setContent(pane);
                notificationViewController = loader.getController();
                tabPane.getTabs().add(tab);
                notificationViewController.registerParentControllerObject(settings, new NotificationGenerator(new MastodonParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)));
            }
            tabPane.getTabs().add((Tab) FXMLLoader.load(getClass().getResource("../layout/test_tab.fxml")));
            tabPane.getTabs().add((Tab) FXMLLoader.load(getClass().getResource("../layout/test_tab.fxml")));
        } catch (Exception e){
            e.printStackTrace();
        }

        postMastodonAPI = new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken);


        this.reloadTask = new ReloadTask(List.of(timelineViewController, notificationViewController));
        this.reloadTask.manualReload();
    }

}
