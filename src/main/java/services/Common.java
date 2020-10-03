package services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.regex.Pattern;

public class Common {
    public static void NotImplementAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR, "そんなものはない", ButtonType.OK);
        alert.setTitle("未実装");
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
    }

    public static void GenericInformationAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
    }

    public static boolean validateURL(String url) {
        // URLとしてあり得る記号のみ許可する
        // TODO: 詳しい人にこれで安全か聞く
        if(url == null) return false;
        return Pattern.compile("^https?://[a-zA-Z0-9/:%#&~=_!'\\$\\?\\(\\)\\.\\+\\*\\-]+$").matcher(url).matches();
    }
    }
