package controller;

import connection.MastodonAPI;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import javafx.stage.Modality;
import javafx.stage.Stage;
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


public class Controller implements Initializable {

    Stage stage;

    MastodonAPI postMastodonAPI;
    private ReloadTask reloadTask;
    private Settings settings;
    private final String myUserName = "user"; // TODO

    private HashMap<String, IContentListController> contentControllers; // TODO: タイムライン以外も複製できるように

    private String inReplyToId;

    @FXML
    private VBox root;

    @FXML
    private TextArea textArea;

    @FXML
    private TabPane tabPane;

    @FXML private WebView webView;

    @FXML private CheckMenuItem userIconVisible;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        reloadTask.manualReload();
    }

    @FXML
    protected void onMenuItemDebug(ActionEvent evt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("開発用情報");
        alert.getDialogPane().setHeaderText("開発用情報");
        var instanceSettings = settings.getInstanceSettings();
        var contentText = "";
        for(var instanceSetting : instanceSettings){
            var hostName = instanceSetting.hostName;
            contentText = contentText + "mastodon host: " + hostName + "\n";
        }
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
            for( var contentController : contentControllers.values() ){
                contentController.iconInvisible(true);
            }
        }
        else {
            for( var contentController : contentControllers.values() ){
                contentController.iconInvisible(false);
            }
        }
    }


    @FXML
    protected void onMenuItemTabSetting(ActionEvent evt) {
        Common.NotImplementAlert();
        if(true) return; // TODO:
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/specific_tab_setting.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.initOwner(this.stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("固有タブの設定");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e){
            e.printStackTrace();
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
        for(var controller : contentControllers.values()){
            if(controller.getClass().equals(TimelineViewController.class)) {
                TimelineViewController timelineViewController = (TimelineViewController) controller;
                timelineViewController.userFilterWordBoxToggle();
            }
        }

        // TODO: 選ばれてるタブのコントローラでやる必要がある
    }

    public void addUserTab(String userId, String username, String hostname, String token){
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
                    new TimelineGenerator(
                            new MastodonTimelineParser(hostname, token, //FIXME: たぶん元のタブからもらってこないと開けない
                                    new UserTimelineGet(hostname, token, userId)
                                    , myUserName)
                    ),
                    new MastodonAPI(settings.getInstanceSettings().get(0).hostName, settings.getInstanceSettings().get(0).accessToken),
                    hostname);
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

        var controllersForReload = new ArrayList();
        // 動的タブ追加のテスト
        try {

            var instanceSettings = settings.getInstanceSettings();

            for(var instanceSetting : instanceSettings){
                var hostname = instanceSetting.hostName;
                var accessToken = instanceSetting.accessToken;
                {
                    var homeTimelineGenerator = new TimelineGenerator(
                            new MastodonTimelineParser(hostname, accessToken,
                                    new HomeTimelineGet(hostname, accessToken), myUserName)
                    );
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                    Tab tab = new Tab("default");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    tab.setContent(pane);
                    TimelineViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(
                            this,
                            homeTimelineGenerator,
                            new MastodonAPI(hostname, accessToken),
                            hostname);
                    controller.registerWebViewOutput(webView);
                    contentControllers.put("Home<"+hostname+">", controller);
                    controllersForReload.add(controller);
                }
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/notification_view.fxml"));
                    Tab tab = new Tab("notification");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    tab.setContent(pane);
                    NotificationViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(
                            this,
                            new NotificationGenerator(
                                    new MastodonNotificationParser(hostname, accessToken, myUserName)
                            ),
                            hostname);
                    contentControllers.put("Notification<"+hostname+">", controller);
                    controllersForReload.add(controller);
                }
                {
                    var localTimelineGenerator = new TimelineGenerator(
                            new MastodonTimelineParser(hostname, accessToken,
                                    new LocalTimelineGet(hostname, accessToken), myUserName)
                    );
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                    Tab tab = new Tab("local");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    tab.setContent(pane);
                    TimelineViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(this,
                            localTimelineGenerator,
                            new MastodonAPI(hostname, accessToken),
                            hostname);
                    controller.registerWebViewOutput(webView);
                    contentControllers.put("Local<"+hostname+">", controller);
                    controllersForReload.add(controller);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        postMastodonAPI = new MastodonAPI(settings.getInstanceSettings().get(0).hostName, settings.getInstanceSettings().get(0).accessToken);


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


        this.reloadTask = new ReloadTask(controllersForReload);
        this.reloadTask.manualReload();
    }

}
