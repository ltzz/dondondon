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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

public class NotificationGenerator {

    MastodonNotificationParser mastodonParser;
    ObservableList<RowContent> data = FXCollections.observableArrayList();
    public NotificationGenerator(MastodonNotificationParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }

    // 汎用通知項目データクラス
    public static class NotificationContent {
        TimelineGenerator.DataSourceInfo dataSourceInfo;
        String username;
        String displayName;
        String contentText;
        String createdAt;
        String avatarURL;

        public NotificationContent(TimelineGenerator.DataSourceInfo dataSourceInfo,
                                   String username, String displayName,
                                   String contentText,
                                   String createdAt,
                                   String avatarURL) {
            this.dataSourceInfo = dataSourceInfo;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.createdAt = createdAt;
            this.avatarURL = avatarURL;
        }
    }

    public static class RowContent {
        public final TimelineGenerator.DataSourceInfo dataSourceInfo;
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty createdAt = new SimpleStringProperty();
        private ObjectProperty userIcon = new SimpleObjectProperty();
        public StringProperty userName = new SimpleStringProperty();

        RowContent(NotificationContent notificationContent){
            this.dataSourceInfo = notificationContent.dataSourceInfo;

            BufferedImage icon = null;
            try {
                // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                icon = ImageIO.read(new URL(notificationContent.avatarURL));
                ImageView iconView = new ImageView(SwingFXUtils.toFXImage(icon, null));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                this.userIcon.set(iconView);
            }catch (Exception e){

            }

            this.userName.set(notificationContent.username + " / " + notificationContent.displayName);
            this.contentText.set(notificationContent.contentText);
            this.createdAt.set(notificationContent.createdAt);
        }

        public ObjectProperty userIconProperty(){ return userIcon; }
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty createdAtProperty(){ return createdAt; }
    }

    public ObservableList<RowContent> createRowContents() {
        var notificationData = mastodonParser.diffNotification();

        for (NotificationContent notification : notificationData) {
            notificationAdd(notification);
        }

        data.sort(Comparator.comparing(notificationContent -> notificationContent.createdAt.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public ObservableList<RowContent> getRowContents(){
        return data;
    }

    public void notificationAdd(NotificationContent notificationContent){
        if(data != null){
            data.add(new RowContent(notificationContent));
        }
    }
}
