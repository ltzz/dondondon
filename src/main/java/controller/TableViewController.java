package controller;

import connection.MastodonAPI;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import misc.Akan;
import timeline.parser.MastodonParser;
import misc.ReloadTask;
import timeline.TimelineGenerator;

public class TableViewController implements Initializable {
    @FXML
    private TableView<TimelineGenerator.RowContent> tableView;

    private ReloadTask reloadTask;
    private TimelineGenerator timelineGenerator;

    public void tableViewSetItems(ObservableList<TimelineGenerator.RowContent> rowContents){
        tableView.setItems(rowContents);
    }

    public void tabRefresh(){
        ObservableList<TimelineGenerator.RowContent> rowContents = timelineGenerator.createTootContents(); // TODO:
        tableViewSetItems(rowContents);
    }

    public void registerWebViewOutput(WebView webView){
        final String twemoji = "<script src=\"https://twemoji.maxcdn.com/v/12.1.5/twemoji.min.js\" integrity=\"sha384-E4PZh8MWwKQ2W7ANni7xwx6TTuPWtd3F8mDRnaMvJssp5j+gxvP2fTsk1GnFg2gG\" crossorigin=\"anonymous\"></script>";
        final String styleString = "<style>html{font-size: 12px;background-color: #2B2B2B; color: #A9B7C6;font-family: Meiryo,\"メイリオ\",'Segoe UI Emoji',sans-serif;font-weight:500;}</style>";
        final String contentHeader = "<!DOCTYPE html><html lang=\"ja\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + twemoji + styleString + "</head><body><div>";
        // final String EMOJI_TEST = "<span style=\"border: 1px #cccccc solid;\">絵文字でねえ🍑💯 &#x1F004</span>";
        final String EMOJI_TEST = "";
        final String twemojiFooter = "<script>twemoji.parse(document.body)</script>";
        final String contentFooter = "<br></div>"+EMOJI_TEST+twemojiFooter+"</body></html>";
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                var tootContent = tableView.getSelectionModel().getSelectedItem();
                var content = tootContent.contentText;
                String htmlString = contentHeader + content + contentFooter;
                WebEngine webEngine = webView.getEngine();
                webEngine.loadContent(htmlString,"text/html");
            }
        });
    }

    public void reloadTaskStart(){
        reloadTask.start();
    }
    public void reloadTaskStop(){
        reloadTask.stop();
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemFavorite = new MenuItem("お気に入り");
        menuItemFavorite.setOnAction((ActionEvent t) -> {
            var selected = tableView.getSelectionModel().getSelectedItem();
            var hostname = selected.hostname;
            var tootId = selected.tootId;

            if( "mastodon".equals(selected.serverType) ) {
                MastodonAPI mastodonAPI = new MastodonAPI(hostname, Akan.TOKEN);
                mastodonAPI.addFavorite(tootId);
            }
        });

        contextMenu.getItems().addAll(menuItemFavorite);

        this.timelineGenerator = new TimelineGenerator(new MastodonParser());
        this.reloadTask = new ReloadTask(tableView, timelineGenerator);

        tabRefresh();

        if(tableView != null) {
            tableView.setOnContextMenuRequested((ContextMenuEvent event) -> {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                event.consume();
            });

            tableView.setRowFactory(new Callback<TableView<TimelineGenerator.RowContent>, TableRow<TimelineGenerator.RowContent>>() {
                @Override
                public TableRow<TimelineGenerator.RowContent> call(TableView<TimelineGenerator.RowContent> tootCellTableView) {
                    var tootCell = new Controller.TootCell();
                    tootCell.getStyleClass().add("toot-row");
                    return tootCell;
                }
            });
        }
    }
}
