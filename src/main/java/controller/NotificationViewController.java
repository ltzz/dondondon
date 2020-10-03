package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;

import services.DataStore;
import timeline.NotificationGenerator;
import timeline.NotificationGenerator.*;

import java.util.Optional;

public class NotificationViewController implements Initializable, IContentListController {
    @FXML
    private TableView<RowContent> tableView;

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

    public void tableViewSetItems(ObservableList<RowContent> rowContents) {
        tableView.setItems(rowContents);
    }

    @Override
    public void reload() {
        ObservableList<RowContent> rowContents = notificationGenerator.createRowContents(); // TODO:
        tableViewSetItems(rowContents);
    }

    public void viewRefresh() {
        reload();
    }


    public static class NotificationCell extends TableRow<RowContent> {
        @Override
        protected void updateItem(RowContent rowContent, boolean empty) {
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
        MenuItem menuItemReply = new MenuItem("返信");

        menuItemUserTimeline.setOnAction((ActionEvent t) -> {
            RowContent selectedNotification = tableView.getSelectionModel().getSelectedItem();
            rootController.addUserTab(selectedNotification.userId, selectedNotification.userName, hostname, selectedNotification.dataOriginInfo.getToken());
        });

        menuItemReply.setOnAction((ActionEvent t) -> {
            RowContent selectedNotification = tableView.getSelectionModel().getSelectedItem();
            String notificationId = selectedNotification.id;
            DataStore dataStore = rootController.dataStore;
            Optional<String> statusId = dataStore.getNotification(hostname, notificationId).get().statusId;
            if(!statusId.isPresent()){
                return;
            }
            Optional<DataStore.TLContent> toot = dataStore.getToot(hostname, statusId.get());
            if(!toot.isPresent()){
                return;
            }
            String acct = toot.get().acct;
            String visiblity = (String) toot.get().instanceSpecificData.getOrDefault("visibility", (Object)"");

            rootController.userReplyInputStart(hostname, statusId.get(), acct, visiblity);
        });

        tableView.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        tableView.setOnMouseClicked((event) -> {
            contextMenu.hide();
        });

        contextMenu.getItems().addAll(menuItemUserTimeline, menuItemReply);
    }

    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {

        contextMenuInit();

        if (tableView != null) {

            ObservableList<TableColumn<RowContent, ?>> columns = tableView.getColumns();
            for (TableColumn<RowContent, ?> column : columns) column.setSortable(false);

            tableView.setRowFactory(new Callback<TableView<RowContent>, TableRow<RowContent>>() {
                @Override
                public TableRow<RowContent> call(TableView<RowContent> tootCellTableView) {
                    NotificationViewController.NotificationCell notificationCell = new NotificationViewController.NotificationCell();
                    notificationCell.getStyleClass().add("notification-row");
                    return notificationCell;
                }
            });
        }
    }
}
