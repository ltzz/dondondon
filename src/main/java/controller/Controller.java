package controller;

import connection.MastodonAPI;
import connection.MastodonAPIParser;
import connection.MultipartFormData;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.Modality;
import javafx.stage.Stage;
import misc.*;

import timeline.MixTimelineGenerator;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;
import timeline.parser.ITimelineGenerator;
import timeline.parser.MastodonNotificationParser;
import timeline.parser.MastodonTimelineParser;
import timeline.parser.timelineEndPoint.HomeTimelineGet;
import timeline.parser.timelineEndPoint.LocalTimelineGet;
import timeline.parser.timelineEndPoint.UserTimelineGet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class Controller implements Initializable {

    Stage stage;

    MastodonAPI postMastodonAPI;
    private ReloadTask reloadTask;
    private Settings settings;
    private final String myUserName = "user"; // TODO

    private HashMap<String, IContentListController> contentControllers; // TODO: タイムライン以外も複製できるように

    BottomForm.FormState formState;

    private ConcurrentHashMap<String, BufferedImage> iconCache;

    @FXML
    private VBox root;

    @FXML
    private TextArea textArea;

    @FXML
    private Text inputTextStatus;

    @FXML
    private TabPane tabPane;
    /*
    @FXML // TODO: SplitPane化試行
    SplitPane splitPane;
    */

    @FXML
    private WebView webView;

    @FXML
    private CheckMenuItem userIconVisible;

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
        List<Settings.InstanceSetting> instanceSettings = settings.getInstanceSettings();
        String contentText = "";
        for (Settings.InstanceSetting instanceSetting : instanceSettings) {
            String hostName = instanceSetting.hostName;
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
        if (userIconVisible.selectedProperty().get()) {
            for (IContentListController contentController : contentControllers.values()) {
                contentController.iconInvisible(true);
            }
        } else {
            for (IContentListController contentController : contentControllers.values()) {
                contentController.iconInvisible(false);
            }
        }
    }


    @FXML
    protected void onMenuItemTabSetting(ActionEvent evt) {
        Common.NotImplementAlert();
        if (true) return; // TODO:
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/specific_tab_setting.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.initOwner(this.stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("固有タブの設定");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
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

    private void userPostEvent() {
        String text = textArea.getText();
        if (!text.isEmpty()) {
            postMastodonAPI.postStatus(text, formState);
            textArea.setText(""); // TODO: 成功時にのみクリア
            formInitialize();
        }
    }

    private void imagePreviewBeforeUploadEvent() {
        File file = ClipboardService.readImage();
        if( file == null ) return;
        try {
            MultipartFormData.FileDto fileDto = UploadImageChooser.readFile(file);

            ButtonType buttonYes = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonNo = new ButtonType("NO", ButtonBar.ButtonData.CANCEL_CLOSE);

            // TODO: ここでクリップボードの画像が見れるように
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "画像をアップロードします。", buttonNo, buttonYes);
            alert.setTitle("画像アップロード");
            Optional<ButtonType> result = alert.showAndWait();

            if( result.isPresent() && result.get() == buttonYes ){
                String output = postMastodonAPI.uploadMedia(fileDto);
                if (output != null && !output.isEmpty()) { // FIXME: 通信OKかどうかをレスポンスで持たす作りにすること
                    MastodonTimelineParser.UploadMediaResponse response = MastodonAPIParser.upload(output);
                    formState.setImageId(response.id);
                    formState.getStatusTexts().add("画像");
                    inputTextStatus.setText(formState.getStatusDisplayText());
                    textArea.setText(textArea.getText() + " " + response.text_url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onMenuItemClearReply(ActionEvent evt) {
        replyModeCancel();
    }

    @FXML
    protected void onMenuItemClearForm(ActionEvent evt) {
        formInitialize();
    }

    @FXML
    protected void onMenuItemUploadImage(ActionEvent evt) {
        try {
            MultipartFormData.FileDto fileDto = UploadImageChooser.choose();
            String output = postMastodonAPI.uploadMedia(fileDto);
            if (output != null && !output.isEmpty()) { // FIXME: 通信OKかどうかをレスポンスで持たす作りにすること
                MastodonTimelineParser.UploadMediaResponse response = MastodonAPIParser.upload(output);
                formState.setImageId(response.id);
                formState.getStatusTexts().add("画像");
                inputTextStatus.setText(formState.getStatusDisplayText());
                textArea.setText(textArea.getText() + " " + response.text_url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void userReplyInputStart(String inReplyToStatusId, String acct) {
        textArea.setText("@" + acct + " ");
        formState.setInReplyToId(inReplyToStatusId);
        textArea.lookup(".content").getStyleClass().add("u-bgLightPinkColor");
        formState.getStatusTexts().add("返信");
        inputTextStatus.setText(formState.getStatusDisplayText());
        textArea.requestFocus();
        int caretPosition = acct.length() + 2; // @と空白で+2
        textArea.positionCaret(caretPosition);
        // TODO: 送信時データ読み込み元ホストに応じてAPI叩く鯖切り替えできるように
    }

    private void formInitialize() {
        formState.initialize();
        textArea.lookup(".content").getStyleClass().remove("u-bgLightPinkColor");
        inputTextStatus.setText(formState.getStatusDisplayText());
    }

    private void replyModeCancel() {
        formState.setInReplyToId(null);
        formState.getStatusTexts().remove("返信");
        textArea.lookup(".content").getStyleClass().remove("u-bgLightPinkColor");
        inputTextStatus.setText(formState.getStatusDisplayText());
    }

    private void userFilterWordBoxToggle() {
        for (IContentListController controller : contentControllers.values()) {
            if (controller.getClass().equals(TimelineViewController.class)) {
                TimelineViewController timelineViewController = (TimelineViewController) controller;
                timelineViewController.userFilterWordBoxToggle();
            }
        }

        // TODO: 選ばれてるタブのコントローラでやる必要がある
    }

    public void addUserTab(String userId, String username, String hostname, String token) {
        try {
            String tabKey = "UserTab<" + userId + ">";
            if (contentControllers.containsKey(tabKey)) return;
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
                                    , myUserName, iconCache)
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initShortcutKey() {
        final KeyCombination postTextAreaKey =
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        final KeyCombination filterWordKey =
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        final KeyCombination pasteKey =
                new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (postTextAreaKey.match(event)) {
                userPostEvent();
            }
        });

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (pasteKey.match(event)) {
                imagePreviewBeforeUploadEvent();
            }
        });

        root.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (filterWordKey.match(event)) {
                userFilterWordBoxToggle();
            }
        });
    }

    private void registerTabContextMenu(ITimelineGenerator timelineGenerator, Tab tab, Pane pane) {
        // Context Menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemTabInfo = new MenuItem("情報");
        MenuItem menuItemTabGraph = new MenuItem("時間ごとの件数");
        menuItemTabInfo.setOnAction((ActionEvent t) -> {
            Common.GenericInformationAlert("情報", String.format("読み込み個数: %d", timelineGenerator.getNumberOfContent()));
        });

        menuItemTabGraph.setOnAction((ActionEvent t) -> {
            Stage stage = new Stage();
            stage.initOwner(this.stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Line Chart");
            TransitionGraph.draw(timelineGenerator.getGeneratorName(), stage, timelineGenerator.getNumberOfContentByHours());
        });


        contextMenu.getItems().addAll(menuItemTabInfo, menuItemTabGraph);
        pane.setOnContextMenuRequested(e ->
                contextMenu.show(pane, e.getScreenX(), e.getScreenY()));
        tab.setContent(pane);
        tab.setContextMenu(contextMenu);
    }

    private void initSpecificTab() {
        List<IContentListController> controllersForReload = new ArrayList<IContentListController>();

        try {

            List<Settings.InstanceSetting> instanceSettings = settings.getInstanceSettings();

            for (Settings.InstanceSetting instanceSetting : instanceSettings) {
                String hostname = instanceSetting.hostName;
                String accessToken = instanceSetting.accessToken;
                {
                    TimelineGenerator homeTimelineGenerator = new TimelineGenerator(
                            new MastodonTimelineParser(hostname, accessToken,
                                    new HomeTimelineGet(hostname, accessToken), myUserName, iconCache)
                    );
                    String generatorName = "Home<" + hostname + ">";
                    homeTimelineGenerator.setGeneratorName(generatorName);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                    Tab tab = new Tab("home");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    registerTabContextMenu(homeTimelineGenerator, tab, pane);

                    tab.setContent(pane);
                    TimelineViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(
                            this,
                            homeTimelineGenerator,
                            new MastodonAPI(hostname, accessToken),
                            hostname);
                    controller.registerWebViewOutput(webView);
                    contentControllers.put(generatorName, controller);
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
                                    new MastodonNotificationParser(hostname, accessToken, myUserName, iconCache)
                            ),
                            hostname);
                    contentControllers.put("Notification<" + hostname + ">", controller);
                    controllersForReload.add(controller);
                }
                {
                    TimelineGenerator localTimelineGenerator = new TimelineGenerator(
                            new MastodonTimelineParser(hostname, accessToken,
                                    new LocalTimelineGet(hostname, accessToken), myUserName, iconCache)
                    );
                    String generatorName = "Local<" + hostname + ">";
                    localTimelineGenerator.setGeneratorName(generatorName);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                    Tab tab = new Tab("local");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    registerTabContextMenu(localTimelineGenerator, tab, pane);
                    tab.setContent(pane);
                    TimelineViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(this,
                            localTimelineGenerator,
                            new MastodonAPI(hostname, accessToken),
                            hostname);
                    controller.registerWebViewOutput(webView);
                    contentControllers.put(generatorName, controller);
                    controllersForReload.add(controller);
                }
            }
            { // 試験的
                // 今のところ2つ以上インスタンスを登録するには設定ファイルを弄る導線しかないため、上級者向け機能として動作する
                // TODO: 2つ以上インスタンス登録するUIを作った場合は、この機能の有効無効をどこで決めるか仕様を定める
                // 2つ以上インスタンスの設定情報がある場合はホームタイムラインを合成したタブを出す
                if (2 <= instanceSettings.size()) {
                    String hostname1 = instanceSettings.get(0).hostName;
                    String accessToken1 = instanceSettings.get(0).accessToken;
                    String hostname2 = instanceSettings.get(1).hostName;
                    String accessToken2 = instanceSettings.get(1).accessToken;
                    ITimelineGenerator mixTimelineGenerator = new MixTimelineGenerator(
                            new TimelineGenerator(
                                    new MastodonTimelineParser(hostname1, accessToken1,
                                            new HomeTimelineGet(hostname1, accessToken1), myUserName, iconCache)
                            ),
                            new TimelineGenerator(
                                    new MastodonTimelineParser(hostname2, accessToken2,
                                            new HomeTimelineGet(hostname2, accessToken2), myUserName, iconCache)
                            )
                    );
                    String generatorName = "Mix<" + hostname1 + "," + hostname2 + ">";
                    mixTimelineGenerator.setGeneratorName(generatorName);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/timeline_view.fxml"));
                    Tab tab = new Tab("mix");
                    tab.setClosable(false);
                    AnchorPane pane = loader.load();
                    registerTabContextMenu(mixTimelineGenerator, tab, pane);
                    tab.setContent(pane);
                    TimelineViewController controller = loader.getController();
                    tabPane.getTabs().add(tab);
                    controller.registerParentControllerObject(
                            this,
                            mixTimelineGenerator,
                            new MastodonAPI(hostname1, accessToken1),
                            hostname1);
                    controller.registerWebViewOutput(webView);
                    contentControllers.put(generatorName, controller);
                    controllersForReload.add(controller);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.reloadTask = new ReloadTask(controllersForReload);
    }

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        settings = new Settings();
        if (true) { // For Developper:　設定保存用
            SettingsLoadOnStart settingsLoadOnStart = new SettingsLoadOnStart(settings);
            settingsLoadOnStart.startSequence();
        }

        iconCache = new ConcurrentHashMap<String, BufferedImage>();

        formState = new BottomForm.FormState();

        contentControllers = new HashMap<>();

        initSpecificTab();

        postMastodonAPI = new MastodonAPI(settings.getInstanceSettings().get(0).hostName, settings.getInstanceSettings().get(0).accessToken);

        initShortcutKey();

        WebEngine webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new BrowserOpenEventListener(webView));
        webView.setContextMenuEnabled(false);
        this.reloadTask.manualReload();
    }

}
