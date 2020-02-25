package controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import misc.BrowserLauncher;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import static timeline.parser.MastodonTimelineParser.validateURL;

public class BrowserOpenEventListener implements ChangeListener<Worker.State>, EventListener {
    private final WebView webView;

    public BrowserOpenEventListener(WebView webView) {
        this.webView = webView;
    }

    @Override
    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
        if (newState == Worker.State.SUCCEEDED) {
            Document document = webView.getEngine().getDocument();
            NodeList anchorList = document.getElementsByTagName("a");
            for (int i = 0; i < anchorList.getLength(); i++) {
                Node node = anchorList.item(i);
                EventTarget eventTarget = (EventTarget) node;
                eventTarget.addEventListener("click", this, false);
            }
        }
    }

    @Override
    public void handleEvent(Event event) {
        HTMLAnchorElement anchorElement = (HTMLAnchorElement) event.getCurrentTarget();
        String href = anchorElement.getHref();
        if (validateURL(href)) {
            BrowserLauncher.launch(href);
        }
        event.preventDefault();
    }
}
