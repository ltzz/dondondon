package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;
import timeline.TimelineGenerator;

public class NotificationViewController implements Initializable {
    @FXML
    private TableView<TimelineGenerator.RowContent> tableView;
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        if(tableView != null) {
            tableView.setRowFactory(new Callback<TableView<TimelineGenerator.RowContent>, TableRow<TimelineGenerator.RowContent>>() {
                @Override
                public TableRow<TimelineGenerator.RowContent> call(TableView<TimelineGenerator.RowContent> tootCellTableView) {
                    var tootCell = new TimelineViewController.TootCell();
                    tootCell.getStyleClass().add("notification-row");
                    return tootCell;
                }
            });
        }
    }
}
