package timeline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import timeline.parser.MastodonNotificationParser;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class NotificationGenerator {

    MastodonNotificationParser mastodonParser;
    private TreeMap<String, RowContent> fetchedContents;
    public NotificationGenerator(MastodonNotificationParser mastodonParser){
        this.mastodonParser = mastodonParser;
        this.fetchedContents = new TreeMap<String, RowContent>();
    }

    // 汎用通知項目データクラス
    public static class NotificationContent {
        TimelineGenerator.DataSourceInfo dataSourceInfo;
        String id;
        String userId;
        String username;
        String displayName;
        String contentText;
        String createdAt;
        BufferedImage avatarIcon;

        public NotificationContent(TimelineGenerator.DataSourceInfo dataSourceInfo,
                                   String id,
                                   String userId,
                                   String username, String displayName,
                                   String contentText,
                                   String createdAt,
                                   BufferedImage avatarIcon) {
            this.dataSourceInfo = dataSourceInfo;
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.createdAt = createdAt;
            this.avatarIcon = avatarIcon;
        }
    }

    public static class RowContent {
        public String id;
        public String userId;
        public String userName;
        public final TimelineGenerator.DataSourceInfo dataSourceInfo;
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty createdAt = new SimpleStringProperty();
        private ObjectProperty userIcon = new SimpleObjectProperty();
        public StringProperty contentTextForColumn = new SimpleStringProperty();

        RowContent(NotificationContent notificationContent){
            this.dataSourceInfo = notificationContent.dataSourceInfo;
            this.id = notificationContent.id;
            this.userId = notificationContent.userId;
            this.userName = notificationContent.username;

            try {
                ImageView iconView = new ImageView(SwingFXUtils.toFXImage(notificationContent.avatarIcon, null));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                this.userIcon.set(iconView);
            }catch (Exception e){

            }

            this.contentTextForColumn.set(notificationContent.username + " / " + notificationContent.displayName);
            this.contentText.set(notificationContent.contentText);
            this.createdAt.set(notificationContent.createdAt);
        }

        public ObjectProperty userIconProperty(){ return userIcon; }
        public StringProperty userNameProperty(){ return contentTextForColumn; }
        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty createdAtProperty(){ return createdAt; }
    }

    public ObservableList<RowContent> createRowContents() {
        var notificationData = mastodonParser.diffNotification();

        for (NotificationContent notification : notificationData) {
            fetchedContents.put(notification.id, new RowContent(notification));
        }

        var fetchedList = fetchedContents.values().stream().collect(Collectors.toList());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }

}
