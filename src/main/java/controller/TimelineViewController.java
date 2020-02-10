package controller;

import connection.MastodonAPI;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import misc.IReload;
import misc.Settings;
import timeline.TimelineGenerator;

public class TimelineViewController implements Initializable, IReload {
    @FXML
    private TableView<TimelineGenerator.RowContent> tableView;

    private Settings settings;
    private TimelineGenerator timelineGenerator;
    private MastodonAPI postMastodonAPI;

    public void tableViewSetItems(ObservableList<TimelineGenerator.RowContent> rowContents){
        tableView.setItems(rowContents);
    }

    @Override
    public void reload() {
        ObservableList<TimelineGenerator.RowContent> rowContents = timelineGenerator.createTootContents(); // TODO:
        tableViewSetItems(rowContents);
    }

    public void viewRefresh(){
        reload();
    }

    public void registerParentControllerObject(Settings settings, TimelineGenerator timelineGenerator, MastodonAPI postMastodonAPI){
        this.settings = settings;
        this.postMastodonAPI = postMastodonAPI;
        this.timelineGenerator = timelineGenerator;
    }

    public void registerWebViewOutput(WebView webView){
        final String twemoji = "<script src=\"https://twemoji.maxcdn.com/v/12.1.5/twemoji.min.js\" integrity=\"sha384-E4PZh8MWwKQ2W7ANni7xwx6TTuPWtd3F8mDRnaMvJssp5j+gxvP2fTsk1GnFg2gG\" crossorigin=\"anonymous\"></script>";
        final String styleString = "<style>html{font-size: 12px;background-color: #2B2B2B; color: #A9B7C6;font-family: Meiryo,\"メイリオ\",'Segoe UI Emoji',sans-serif;font-weight:500;}</style>";
        final String contentHeader = "<!DOCTYPE html><html lang=\"ja\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + twemoji + styleString + "</head><body><div>";
        // final String EMOJI_TEST = "<span style=\"border: 1px #cccccc solid;\">絵文字でねえ🍑💯 &#x1F004</span>";
        //final String EMOJI_TEST = "\uD842\uDFB7野屋";
        final String EMOJI_TEST = "";
        final String twemojiFooter = "<script>twemoji.parse(document.body)</script>";
        final String contentFooter = "<br></div>"+toCharacterReference(EMOJI_TEST)+twemojiFooter+"</body></html>";
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                var tootContent = tableView.getSelectionModel().getSelectedItem();
                var contentImage = "<img src=\"" + tootContent.contentImageURL + "\" />";
                var contentHtml = tootContent.contentHtml;
                String htmlString = contentHeader + toCharacterReference(contentHtml) + contentImage + contentFooter;
                WebEngine webEngine = webView.getEngine();
                webEngine.setUserStyleSheetLocation(getClass().getResource("webview.css").toString());
                webEngine.loadContent(htmlString);
            }
        });
    }

    public static class TootCell extends TableRow<TimelineGenerator.RowContent> {
        @Override
        protected void updateItem(TimelineGenerator.RowContent rowContent, boolean empty){
            if( rowContent != null && "true".equals(rowContent.favorited) ) {
                this.getStyleClass().add("-favorited");
            }
            else{
                this.getStyleClass().remove("-favorited");
            }
            super.updateItem(rowContent, empty);
        }
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemFavorite = new MenuItem("お気に入り");
        MenuItem menuItemReply = new MenuItem("返信");
        menuItemFavorite.setOnAction((ActionEvent t) -> {
            var selectedToot = tableView.getSelectionModel().getSelectedItem();
            var hostname = selectedToot.dataSourceInfo.hostname;
            var statusId = selectedToot.dataSourceInfo.statusId;

            if( "mastodon".equals(selectedToot.dataSourceInfo.serverType) ) {
                // TODO: データ読み込み元ホストに応じてAPI叩く鯖切り替え
                postMastodonAPI.addFavorite(statusId);
            }
        });

        menuItemReply.setOnAction((ActionEvent t) -> {
            var selectedToot = tableView.getSelectionModel().getSelectedItem();
            var statusId = selectedToot.dataSourceInfo.statusId;

            System.out.println("実装しとらんわ");
        });

        contextMenu.getItems().addAll(menuItemFavorite, menuItemReply);

        if(tableView != null) {

            var columns = tableView.getColumns();
            for( var column : columns ) column.setSortable(false);

            tableView.setOnContextMenuRequested((ContextMenuEvent event) -> {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                event.consume();
            });

            tableView.setRowFactory(new Callback<TableView<TimelineGenerator.RowContent>, TableRow<TimelineGenerator.RowContent>>() {
                @Override
                public TableRow<TimelineGenerator.RowContent> call(TableView<TimelineGenerator.RowContent> tootCellTableView) {
                    var tootCell = new TootCell();
                    tootCell.getStyleClass().add("toot-row");
                    return tootCell;
                }
            });
        }
    }


    String toCharacterReference(String str) {
        int len = str.length();
        int[] codePointArray = new int[str.codePointCount(0, len)];

        for (int i = 0, num = 0; i < len; i = str.offsetByCodePoints(i, 1)) {
            codePointArray[num] = str.codePointAt(i);
            num += 1;
        }

        StringBuffer stringBuffer = new StringBuffer();
        for (int value : codePointArray){
            if(value >= 0x10000){
                stringBuffer.append("&#x" + (Integer.toHexString(value)) + ";");
            }else {
                stringBuffer.append(Character.toChars(value));
            }
        }
        return stringBuffer.toString();
    }
}
