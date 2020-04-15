package misc;

import javafx.stage.FileChooser;

import java.io.File;

public class UploadImageChooser {
    public static void choose(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("アップロード画像を選択");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("画像ファイル", "*.jpg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("すべてのファイル", "*.*")
        );
        File file = fileChooser.showOpenDialog(null); // TODO: stage渡す

        if(file != null) {
            System.out.println(file.getPath());
            String[] strings = file.getPath().split("\\.");
            String ext = strings[strings.length - 1];
            String mimeType = null;
            if(ext.equals("jpg")){
                mimeType = "image/jpeg";
            }
            if(ext.equals("png")){
                mimeType = "image/png";
            }
            if(ext.equals("gif")){
                mimeType = "image/gif";
            }
            System.out.println(mimeType);
        }
    }
}
