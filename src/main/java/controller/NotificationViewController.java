package controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import timeline.NotificationGenerator;
import timeline.parser.MastodonParser;

public class NotificationViewController implements Initializable {
    @FXML
    private TableView<NotificationGenerator.RowContent> tableView;

    private NotificationGenerator notificationGenerator;

    public void tableViewSetItems(ObservableList<NotificationGenerator.RowContent> rowContents){
        tableView.setItems(rowContents);
    }

    public void viewRefresh(){
        ObservableList<NotificationGenerator.RowContent> rowContents = notificationGenerator.createNotificationContents(); // TODO:
        tableViewSetItems(rowContents);
    }


    public static class NotificationCell extends TableRow<NotificationGenerator.RowContent> {
        @Override
        protected void updateItem(NotificationGenerator.RowContent rowContent, boolean empty){
            super.updateItem(rowContent, empty);
        }
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {


        this.notificationGenerator = new NotificationGenerator(new MastodonParser());

        viewRefresh(); // FIXME: 起動時にしか通知を読み込んでないので、リロード時にも読むようにする

        if(tableView != null) {
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
