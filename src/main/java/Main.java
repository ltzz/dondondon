import controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import misc.MastodonAuth;
import misc.Settings;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("layout/ui_main.fxml"));
        primaryStage.setTitle("${CLIENT_NAME}");
        var scene = new Scene(root, 600, 500);
        scene.getStylesheets().add("dark.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        // こっちのスレッドのアプリケーションはいったん無効化
        // MainWindow mainWindow = new MainWindow("test", 400, 500);


        Settings settings = new Settings();

        var controller = new Controller();

        // settings.save();
        // settings.load();

        if(false) { // For Developper:　token取得用
            var mastodonAuth = new MastodonAuth();
            MastodonAuth.ClientRegisterResponse clientResponse = mastodonAuth.registerClient();
            MastodonAuth.AccessTokenResponse accessTokenResponse = mastodonAuth.getAccessToken(clientResponse.client_id, clientResponse.client_secret);
            System.out.println(accessTokenResponse.access_token);
            // 今の所設定保存機能を実装していないので、不足してる諸々はAkan.javaに書いていく
        }

    }


    public static void main(String[] args) {
        launch(args);
    }
}
