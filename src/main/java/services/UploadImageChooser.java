package services;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadImageChooser {

    public static final class FileDto {
        public final String fileName;
        public final String filePath;
        public final String mimeType;
        public final byte[] bytes;

        public FileDto(String fileName, String filePath, String mimeType, byte[] bytes) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.mimeType = mimeType;
            this.bytes = bytes;
        }
    }

    public static FileDto choose() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("アップロード画像を選択");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("画像ファイル", "*.jpg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("すべてのファイル", "*.*")
        );
        File file = fileChooser.showOpenDialog(null); // TODO: stage渡す
        return readFile(file);
    }

    public static FileDto readFile(File file) throws IOException {
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
            return new FileDto(file.getName(), path.toAbsolutePath().toString(), mimeType, fileData);
        }
        return new FileDto("", "", "", new byte[0]);
    }
}
