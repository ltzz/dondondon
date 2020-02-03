import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BrowserLauncher {

    static void launch(String URL) {
        Desktop desktop = Desktop.getDesktop();
        try {
            URI uri = new URI(URL);
            desktop.browse(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
