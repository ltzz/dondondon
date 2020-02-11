package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import misc.Common;
import misc.Settings;
import misc.SettingsLoadOnStart;
import misc.Version;

import timeline.NotificationGenerator;
import timeline.TimelineGenerator;
import timeline.parser.MastodonNotificationParser;
import timeline.parser.MastodonTimelineParser;
import timeline.parser.timelineEndPoint.HomeTimelineGet;
import timeline.parser.timelineEndPoint.LocalTimelineGet;
import timeline.parser.timelineEndPoint.UserTimelineGet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Controller implements Initializable {

    MastodonAPI postMastodonAPI;
    private ReloadTask reloadTask;
    private Settings settings;

    private TimelineViewController homeTimelineViewController;
    private NotificationViewController notificationViewController;
    private TimelineViewController localTimelineViewController;
    private HashMap<String, TimelineViewController> contentControllers; // TODO: タイムライン以外も複製できるように

    private String inReplyToId;

    @FXML
    private VBox root;

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
    protected void onMenuItemDebug(ActionEvent evt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("開発用情報");
        alert.getDialogPane().setHeaderText("開発用情報");
        var hostName = settings.getInstanceSetting().hostName;
        var contentText = "mastodon host: " + hostName + "\n 今のところ1つしか登録できない";
        alert.getDialogPane().setContentText(contentText);
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
        System.out.println(button.toString());
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
            homeTimelineViewController.iconInvisible(true);
            notificationViewController.iconInvisible(true);
            localTimelineViewController.iconInvisible(true);
        }
        else {
            homeTimelineViewController.iconInvisible(false);
            notificationViewController.iconInvisible(false);
            localTimelineViewController.iconInvisible(false);
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
        userPostEvent();
    }

    private void userPostEvent(){
        String text = textArea.getText();
        if(!text.isEmpty()) {
            postMastodonAPI.postStatus(text, inReplyToId);
            textArea.setText(""); // TODO: 成功時にのみクリア
            replyModeCancel();
        }
    }

    @FXML
    protected void onMenuItemClearReply(ActionEvent evt) {
        replyModeCancel();
    }

    @FXML
    protected void onMenuItemUploadImage(ActionEvent evt) {
        Common.NotImplementAlert();
    }

    public void userReplyInputStart(String inReplyToStatusId, String acct ){
        textArea.setText("@" + acct + " ");
        inReplyToId = inReplyToStatusId;
        textArea.lookup(".content").getStyleClass().add("u-bgLightPinkColor");
        textArea.requestFocus();
        var caretPosition = acct.length() + 2; // @と空白で+2
        textArea.positionCaret(caretPosition);
        // TODO: 送信時データ読み込み元ホストに応じてAPI叩く鯖切り替えできるように
    }

    private void replyModeCancel(){
        inReplyToId = null;
        textArea.lookup(".content").getStyleClass().remove("u-bgLightPinkColor");
    }

    private void userFilterWordBoxToggle(){
        homeTimelineViewController.userFilterWordBoxToggle();
        // TODO: 選ばれてるタブのコントローラでやる必要がある
    }

    public void addUserTab(String userId, String username){
        try {
            var tabKey = "UserTab<"+userId+">";
            if( contentControllers.containsKey(tabKey) ) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
            Tab tab = new Tab("user: " + username);
            AnchorPane pane = loader.load();
            tab.setContent(pane);
            TimelineViewController timelineViewController = loader.getController();
            tabPane.getTabs().add(tab);
            timelineViewController.registerParentControllerObject(this,
                    settings,
                    new TimelineGenerator(
                            new MastodonTimelineParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken,
                                    new UserTimelineGet(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken, userId))
                    ),
                    new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken));
            timelineViewController.registerWebViewOutput(webView);
            contentControllers.put(tabKey, timelineViewController);
            // 閉じたときにコントローラの登録を外す
            tab.setClosable(true);
            tab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event t) {
                    contentControllers.remove(tabKey);
                }
            });
            // リロードタスクでロードしないので手動読み込み
            timelineViewController.reload();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        settings = new Settings();
        if(true) { // For Developper:　設定保存用
            SettingsLoadOnStart settingsLoadOnStart = new SettingsLoadOnStart(settings);
            settingsLoadOnStart.startSequence();
        }

        contentControllers = new HashMap<>();

        // 動的タブ追加のテスト
        try {
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                Tab tab = new Tab("default");
                tab.setClosable(false);
                AnchorPane pane = loader.load();
                tab.setContent(pane);
                homeTimelineViewController = loader.getController();
                tabPane.getTabs().add(tab);
                homeTimelineViewController.registerParentControllerObject(
                        this,
                        settings,
                        new TimelineGenerator(
                                new MastodonTimelineParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken,
                                new HomeTimelineGet(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken))
                        ),
                        new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken));
                homeTimelineViewController.registerWebViewOutput(webView);
            }
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/notification_view.fxml"));
                Tab tab = new Tab("notification");
                tab.setClosable(false);
                AnchorPane pane = loader.load();
                tab.setContent(pane);
                notificationViewController = loader.getController();
                tabPane.getTabs().add(tab);
                notificationViewController.registerParentControllerObject(
                        this,
                        settings,
                        new NotificationGenerator(
                                new MastodonNotificationParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken)
                        ));
            }
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                Tab tab = new Tab("local");
                tab.setClosable(false);
                AnchorPane pane = loader.load();
                tab.setContent(pane);
                localTimelineViewController = loader.getController();
                tabPane.getTabs().add(tab);
                localTimelineViewController.registerParentControllerObject(this,
                        settings,
                        new TimelineGenerator(
                                new MastodonTimelineParser(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken,
                                new LocalTimelineGet(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken))
                        ),
                        new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken));
                localTimelineViewController.registerWebViewOutput(webView);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        postMastodonAPI = new MastodonAPI(settings.getInstanceSetting().hostName, settings.getInstanceSetting().accessToken);


        final KeyCombination postTextAreaKey =
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        final KeyCombination filterWordKey =
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);


        textArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(postTextAreaKey.match(event)) {
                userPostEvent();
            }
        });

        root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(filterWordKey.match(event)){
                userFilterWordBoxToggle();
            }
        });


        this.reloadTask = new ReloadTask(List.of(homeTimelineViewController, notificationViewController, localTimelineViewController));
        this.reloadTask.manualReload();
    }

}
