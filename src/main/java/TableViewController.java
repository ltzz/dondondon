import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

public class TableViewController implements Initializable {
    @FXML
    private TableView<TimelineGenerator.TootContent> tableView;

    private WebView webView;
    private ReloadTask reloadTask;
    private TimelineGenerator timelineGenerator;

    public void tableViewSetItems(ObservableList<TimelineGenerator.TootContent> tootContents){
        tableView.setItems(tootContents);
    }

    public void tabRefresh(){
        ObservableList<TimelineGenerator.TootContent> tootContents = timelineGenerator.createTootContents(); // TODO:
        tableViewSetItems(tootContents);
    }

    public void registerWebViewOutput(WebView webView){
        final String contentHeader = "<!DOCTYPE html><html lang=\"ja\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-16\"><style>*{background-color: #cccccc;font-family: Meiryo,\"メイリオ\",'Segoe UI Emoji',sans-serif;font-weight:500;}</style></head><body><div>";
        final String EMOJI_TEST = "<span style=\"border: 1px #cccccc solid;\">絵文字でねえ🍑💯</span>";
        final String contentFooter = "<br></div>"+EMOJI_TEST+"</body></html>";
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                var tootContent = tableView.getSelectionModel().getSelectedItem();
                String htmlString = contentHeader + tootContent.contentText.get() + contentFooter;
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

        this.timelineGenerator = new TimelineGenerator(new Mastodon());
        this.reloadTask = new ReloadTask(tableView, timelineGenerator);

        ObservableList<TimelineGenerator.TootContent> tootContents = timelineGenerator.createTootContents(); // TODO:
        tableViewSetItems(tootContents);

        if(tableView != null) {

            tableView.setRowFactory(new Callback<TableView<TimelineGenerator.TootContent>, TableRow<TimelineGenerator.TootContent>>() {
                @Override
                public TableRow<TimelineGenerator.TootContent> call(TableView<TimelineGenerator.TootContent> tootCellTableView) {
                    var tootCell = new Controller.TootCell();
                    tootCell.getStyleClass().add("toot-row");
                    return tootCell;
                }
            });
        }
    }
}
