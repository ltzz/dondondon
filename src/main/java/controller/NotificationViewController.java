package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;

import timeline.NotificationGenerator;

public class NotificationViewController implements Initializable, IContentListController {
    @FXML
    private TableView<NotificationGenerator.RowContent> tableView;

    private Controller rootController;
    private NotificationGenerator notificationGenerator;
    String hostname;

    @FXML
    private TableColumn iconCol;

    public void iconInvisible(boolean value) {
        if (value) {
            iconCol.getStyleClass().add("u-hidden");
        } else {
            iconCol.getStyleClass().remove("u-hidden");
        }
    }

    public void tableViewSetItems(ObservableList<NotificationGenerator.RowContent> rowContents) {
        tableView.setItems(rowContents);
    }

    @Override
    public void reload() {
        ObservableList<NotificationGenerator.RowContent> rowContents = notificationGenerator.createRowContents(); // TODO:
        tableViewSetItems(rowContents);
    }

    public void viewRefresh() {
        reload();
    }


    public static class NotificationCell extends TableRow<NotificationGenerator.RowContent> {
        @Override
        protected void updateItem(NotificationGenerator.RowContent rowContent, boolean empty) {
            super.updateItem(rowContent, empty);
        }
    }

    public void registerParentControllerObject(Controller rootController, NotificationGenerator notificationGenerator, String hostname) {
        this.rootController = rootController;
        this.notificationGenerator = notificationGenerator;
        this.hostname = hostname;
    }

    private void contextMenuInit() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemUserTimeline = new MenuItem("このユーザーのタイムラインを見る");
        menuItemUserTimeline.setOnAction((ActionEvent t) -> {
            NotificationGenerator.RowContent selectedNotification = tableView.getSelectionModel().getSelectedItem();
            rootController.addUserTab(selectedNotification.userId, selectedNotification.userName, hostname, selectedNotification.dataOriginInfo.getToken());
        });

        tableView.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        tableView.setOnMouseClicked((event) -> {
            contextMenu.hide();
        });

        contextMenu.getItems().addAll(menuItemUserTimeline);
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        contextMenuInit();

        if (tableView != null) {

            ObservableList<TableColumn<NotificationGenerator.RowContent, ?>> columns = tableView.getColumns();
            for (TableColumn<NotificationGenerator.RowContent, ?> column : columns) column.setSortable(false);

            tableView.setRowFactory(new Callback<TableView<NotificationGenerator.RowContent>, TableRow<NotificationGenerator.RowContent>>() {
                @Override
                public TableRow<NotificationGenerator.RowContent> call(TableView<NotificationGenerator.RowContent> tootCellTableView) {
                    NotificationViewController.NotificationCell notificationCell = new NotificationViewController.NotificationCell();
                    notificationCell.getStyleClass().add("notification-row");
                    return notificationCell;
                }
            });
        }
    }
}
