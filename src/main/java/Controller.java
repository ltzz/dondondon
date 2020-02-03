import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller implements Initializable {
    @FXML private TableView<TootContent> tableView;

    @FXML private TableColumn<TootContent, String> userNameCol;
    @FXML private TableColumn<TootContent, String> contentTextCol;
    @FXML private TableColumn<TootContent, String> contentDateCol;

    public class TootContent{
        public String userName;
        public String contentText;
        public String contentDate;

        TootContent(String UserName, String contentText, String contentDate){
            this.userName = UserName;
            this.contentText = contentText;
            this.contentDate = contentDate; // TODO
        }
        public String getUserName(){ return userName; }
        public String getContentText(){ return contentText; }
        public String getContentDate(){ return contentDate; }
        public void setName(String userName){ this.userName = userName; }
        public void setHome(String contentText){ this.contentText = contentText; }
        public void setAge(String contentDate){ this.contentDate = contentDate; }
    }

    ObservableList<TootContent> contentList = FXCollections.observableArrayList(
            // TODO: image viewでuser icon
            new TootContent("user1", "aaa", "2020-01-01"),
            new TootContent("user2", "bbb", ""),
            new TootContent("user3", "あああ", "")
    );

    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle bundle) {
        change();
    }

    public void change(){

        if(tableView != null) {
            tableView.itemsProperty().setValue(contentList);
            tableView.setItems(contentList);
            userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
            contentTextCol.setCellValueFactory(new PropertyValueFactory<>("contentText"));
            contentDateCol.setCellValueFactory(new PropertyValueFactory<>("contentDate"));
        }
    }
}
