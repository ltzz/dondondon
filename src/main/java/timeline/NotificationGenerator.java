package timeline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class NotificationGenerator {

    private String generatorName;
    private DataStore dataStore;

    public NotificationGenerator(String generatorName, DataStore dataStore) {
        this.generatorName = generatorName;
        this.dataStore = dataStore;
    }

    public static class RowContent {
        public String id;
        public String userId;
        public String userName;
        public final TimelineGenerator.DataOriginInfo dataOriginInfo;
        public Date date;
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty dateForColumn = new SimpleStringProperty();
        private ObjectProperty<ImageView> userIcon = new SimpleObjectProperty<ImageView>();
        public StringProperty contentTextForColumn = new SimpleStringProperty();

        RowContent(DataStore.NotificationContent notificationContent) {
            this.dataOriginInfo = notificationContent.dataOriginInfo;
            this.id = notificationContent.id;
            this.userId = notificationContent.userId;
            this.userName = notificationContent.username;

            try {
                ImageView iconView = new ImageView(SwingFXUtils.toFXImage(notificationContent.avatarIcon, null));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                this.userIcon.set(iconView);
            } catch (Exception e) {

            }


            this.date = notificationContent.createdAt;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            this.dateForColumn.set(simpleDateFormat.format(notificationContent.createdAt));

            this.contentTextForColumn.set(notificationContent.username + " / " + notificationContent.displayName);
            this.contentText.set(notificationContent.contentText);

        }

        public ObjectProperty<ImageView> userIconProperty() {
            return userIcon;
        }

        public StringProperty userNameProperty() {
            return contentTextForColumn;
        }

        public StringProperty contentTextProperty() {
            return contentText;
        }

        public StringProperty dateProperty() {
            return dateForColumn;
        }
    }

    public ObservableList<RowContent> createRowContents() {
        List<DataStore.NotificationContent> fetchedList = dataStore.getNotificationContentList(generatorName);
        List<RowContent> rowContentList = fetchedList.stream().map(RowContent::new).collect(Collectors.toList());
        return FXCollections.observableArrayList(rowContentList);
    }

}
