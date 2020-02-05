import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class Controller implements Initializable {

    TimelineGenerator timelineGenerator;
    ReloadTask reloadTask;

    @FXML private TableView<TimelineGenerator.TootContent> tableView;
    @FXML private WebView webView;

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        ObservableList<TimelineGenerator.TootContent> tootContents = timelineGenerator.createTootContents(); // TODO:
        tableView.setItems(tootContents);
    }

    @FXML
    protected void onMenuItemVersion(ActionEvent evt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("バージョン情報");
        alert.getDialogPane().setHeaderText("バージョン");
        alert.getDialogPane().setContentText(Version.versionString());
        ButtonType button = alert.showAndWait().orElse(ButtonType.OK);
        System.out.println(button.toString());

    }

    @FXML
    protected void onMenuItemReloadPeriodNone(ActionEvent evt) {
        reloadTask.stop();
    }

    @FXML
    protected void onMenuItemReloadPeriod1Min(ActionEvent evt) {
        reloadTask.start();
    }


    public static class TootCell extends TableRow<TimelineGenerator.TootContent> {
        @Override
        protected void updateItem(TimelineGenerator.TootContent tootContent, boolean empty){
            super.updateItem(tootContent, empty);
        }
    }

    // TODO: image viewでuser icon
    // TODO: filter ██
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        this.timelineGenerator = new TimelineGenerator(new Mastodon());
        this.reloadTask = new ReloadTask(tableView,timelineGenerator);
        WebEngine webEngine = webView.getEngine();

        final String contentHeader = "<!DOCTYPE html><html lang=\"ja\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-16\"></head><body><style>span{font-family: 'Segoe UI Emoji';}</style><div>";
        final String EMOJI_TEST = "<span style=\"border: 1px #cccccc solid;\">絵文字でねえ🍑💯</span>";
        final String contentFooter = "<br></div>"+EMOJI_TEST+"</body></html>";
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                var tootContent = tableView.getSelectionModel().getSelectedItem();
                webEngine.loadContent(contentHeader + tootContent.contentText.get() + contentFooter,"text/html");
            }
        });

        if(tableView != null) {

            tableView.setRowFactory(new Callback<TableView<TimelineGenerator.TootContent>, TableRow<TimelineGenerator.TootContent>>() {
                @Override
                public TableRow<TimelineGenerator.TootContent> call(TableView<TimelineGenerator.TootContent> tootCellTableView) {
                    var tootCell = new TootCell();
                    tootCell.getStyleClass().add("toot-row");
                    return tootCell;
                }
            });
            ObservableList<TimelineGenerator.TootContent> tootContents = timelineGenerator.createTootContents(); // TODO:
            tableView.setItems(tootContents);

        }

    }

}
