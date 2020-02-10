package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import misc.IReload;
import misc.Settings;
import timeline.NotificationGenerator;

public class NotificationViewController implements Initializable, IReload {
    @FXML
    private TableView<NotificationGenerator.RowContent> tableView;

    private Settings settings;
    private NotificationGenerator notificationGenerator;

    @FXML
    private TableColumn iconCol;

    public void iconInvisible(boolean value){
        if(value) {
            iconCol.getStyleClass().add("-iconHidden");
        }
        else {
            iconCol.getStyleClass().remove("-iconHidden");
        }
    }

    public void tableViewSetItems(ObservableList<NotificationGenerator.RowContent> rowContents){
        tableView.setItems(rowContents);
    }

    @Override
    public void reload() {
        ObservableList<NotificationGenerator.RowContent> rowContents = notificationGenerator.createRowContents(); // TODO:
        tableViewSetItems(rowContents);
    }

    public void viewRefresh(){
        reload();
    }


    public static class NotificationCell extends TableRow<NotificationGenerator.RowContent> {
        @Override
        protected void updateItem(NotificationGenerator.RowContent rowContent, boolean empty){
            super.updateItem(rowContent, empty);
        }
    }

    public void registerParentControllerObject(Settings settings, NotificationGenerator notificationGenerator){
        this.settings = settings;
        this.notificationGenerator = notificationGenerator;
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        if(tableView != null) {

            var columns = tableView.getColumns();
            for( var column : columns ) column.setSortable(false);

            tableView.setRowFactory(new Callback<TableView<NotificationGenerator.RowContent>, TableRow<NotificationGenerator.RowContent>>() {
                @Override
                public TableRow<NotificationGenerator.RowContent> call(TableView<NotificationGenerator.RowContent> tootCellTableView) {
                    var notificationCell = new NotificationViewController.NotificationCell();
                    notificationCell.getStyleClass().add("notification-row");
                    return notificationCell;
                }
            });
        }
    }
}
