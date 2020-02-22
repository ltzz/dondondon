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
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationGenerator {

    MastodonNotificationParser mastodonParser;
    private TreeMap<String, RowContent> fetchedContents;
    public NotificationGenerator(MastodonNotificationParser mastodonParser){
        this.mastodonParser = mastodonParser;
        this.fetchedContents = new TreeMap<String, RowContent>();
    }

    // 汎用通知項目データクラス
    public static class NotificationContent {
        TimelineGenerator.DataOriginInfo dataOriginInfo;
        String id;
        String userId;
        String username;
        String displayName;
        String contentText;
        Date createdAt;
        BufferedImage avatarIcon;

        public NotificationContent(TimelineGenerator.DataOriginInfo dataOriginInfo,
                                   String id,
                                   String userId,
                                   String username, String displayName,
                                   String contentText,
                                   Date createdAt,
                                   BufferedImage avatarIcon) {
            this.dataOriginInfo = dataOriginInfo;
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
        public final TimelineGenerator.DataOriginInfo dataOriginInfo;
        public Date date;
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty dateForColumn = new SimpleStringProperty();
        private ObjectProperty<ImageView> userIcon = new SimpleObjectProperty<ImageView>();
        public StringProperty contentTextForColumn = new SimpleStringProperty();

        RowContent(NotificationContent notificationContent){
            this.dataOriginInfo = notificationContent.dataOriginInfo;
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


            this.date = notificationContent.createdAt;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            this.dateForColumn.set(simpleDateFormat.format(notificationContent.createdAt));

            this.contentTextForColumn.set(notificationContent.username + " / " + notificationContent.displayName);
            this.contentText.set(notificationContent.contentText);

        }

        public ObjectProperty<ImageView> userIconProperty(){ return userIcon; }
        public StringProperty userNameProperty(){ return contentTextForColumn; }
        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty dateProperty(){ return dateForColumn; }
    }

    public ObservableList<RowContent> createRowContents() {
        List<NotificationGenerator.NotificationContent> notificationData = mastodonParser.diffNotification();

        for (NotificationContent notification : notificationData) {
            fetchedContents.put(notification.id, new RowContent(notification));
        }

        List<RowContent> fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }

}
