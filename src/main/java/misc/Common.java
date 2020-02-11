package misc;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Common {
    public static void NotImplementAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR, "そんなものはない", ButtonType.OK);
        alert.setTitle("未実装");
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
    }
}
