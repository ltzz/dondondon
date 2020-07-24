package misc;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;

import javax.imageio.ImageIO;
import java.io.File;

public class ClipboardService {
    public static File readImage() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasImage()) {
            return null;
        }
        try {
            File file = File.createTempFile("dondondon_upload_tmp", ".png");
            Image image = clipboard.getImage();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
