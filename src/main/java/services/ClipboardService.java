package services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Optional;

public class ClipboardService {
    public static File readImage() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasImage() && !clipboard.hasFiles()) {
            return null;
        }

        if(clipboard.hasImage()) {
            try {
                File file = File.createTempFile("dondondon_upload_tmp", ".png");
                Image image = clipboard.getImage();
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(clipboard.hasFiles()){
            System.out.println(clipboard.getFiles());
            Optional<File> file = clipboard.getFiles().stream().findFirst();
            return file.orElse(null);
        }
        return null;
    }
}
