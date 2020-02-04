import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.util.HashSet;

public class Controller implements Initializable {

    Mastodon mastodon;

    @FXML private TableView<TootContent> tableView;

    @FXML
    protected void onMenuItemReload(ActionEvent evt) {
        ObservableList<TootContent> tootContents = createTootContents(); // TODO:
        tableView.setItems(tootContents);
    }

    public static class TootContent{
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        TootContent(String userName, String contentText, String contentDate){
            this.userName.set(userName);
            this.contentText.set(contentText);
            this.contentDate.set(contentDate); // TODO
        }
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    public static class TootCell extends TableRow<TootContent> {
        @Override
        protected void updateItem(TootContent tootContent, boolean empty){
            super.updateItem(tootContent, empty);
        }
    }

    ObservableList<TootContent> data = FXCollections.observableArrayList();;

            // TODO: image viewでuser icon
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        this.mastodon = new Mastodon();
        if(tableView != null) {

            tableView.setRowFactory(new Callback<TableView<TootContent>, TableRow<TootContent>>() {
                @Override
                public TableRow<TootContent> call(TableView<TootContent> tootCellTableView) {
                    return new TootCell();
                }
            });
            ObservableList<TootContent> tootContents = createTootContents(); // TODO:
            tableView.setItems(tootContents);

        }

    }

    public ObservableList<TootContent> createTootContents(){
        var webRequest = new WebRequest();
        var timelineData = mastodon.diffTimeline();

        for (Mastodon.TLContent tldata : timelineData) {
            timelineAdd(tldata.username, tldata.contentText, tldata.date);
        }
        return data;
    }

    public void timelineAdd(String username, String contentText, String contentDate){
        if(data != null){
            data.add(new TootContent(username, contentText, contentDate));
        }
    }
}
