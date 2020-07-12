package misc;

import connection.MultipartFormData;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadImageChooser {
    public static MultipartFormData.FileDto choose() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("アップロード画像を選択");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("画像ファイル", "*.jpg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("すべてのファイル", "*.*")
        );
        File file = fileChooser.showOpenDialog(null); // TODO: stage渡す

        if (file != null && file.isFile()) {
            System.out.println(file.getPath());
            String[] strings = file.getPath().split("\\.");
            String ext = strings[strings.length - 1];
            String mimeType = null;
            if (ext.equals("jpg")) {
                mimeType = "image/jpeg";
            }
            if (ext.equals("png")) {
                mimeType = "image/png";
            }
            if (ext.equals("gif")) {
                mimeType = "image/gif";
            }
            System.out.println(mimeType);
            Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
            byte[] fileData = Files.readAllBytes(path);
            return new MultipartFormData.FileDto(file.getName(), mimeType, fileData);
        }
        return new MultipartFormData.FileDto("", "", new byte[0]);
    }
}
