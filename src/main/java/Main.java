import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        MainWindow mainWindow = new MainWindow("test", 400, 500);


        Settings settings = new Settings();

        var controller = new Controller();


        var webRequest = new WebRequest();


        // settings.save();
        // settings.load();

        webRequest.getTimeline();
        // develop: webRequestSample.registerClient();
        // String token = webRequest.getAccessToken();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
