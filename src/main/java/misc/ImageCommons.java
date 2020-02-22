package misc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class ImageCommons {
    public static BufferedImage resizeImage(BufferedImage bufferedImage, int resizeWidth, int resizeHeight) throws IOException {
        if(bufferedImage.getWidth() < resizeWidth && bufferedImage.getHeight() < resizeHeight){
            return bufferedImage;
        }
        Image tmp = bufferedImage.getScaledInstance(resizeWidth, resizeHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    public static byte[] readImageAsByte(URL url) {
        try {
            BufferedImage bufferedImage = resizeImage(ImageIO.read(url), 64, 64);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);

            byteArrayOutputStream.flush();
            byte[] buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return buffer;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage readImage(byte[] buffer) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(buffer);
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        return bufferedImage;
    }
}
