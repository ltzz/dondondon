import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("layout/ui_main.fxml"));
        primaryStage.setTitle("${CLIENT_NAME}");
        var scene = new Scene(root, 600, 500);
        scene.getStylesheets().add("dark.css");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(300);
        primaryStage.show();

        // こっちのスレッドのアプリケーションはいったん無効化
        // MainWindow mainWindow = new MainWindow("test", 400, 500);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
