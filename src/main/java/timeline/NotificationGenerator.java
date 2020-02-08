package timeline;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timeline.parser.MastodonParser;

import java.util.Collections;
import java.util.Comparator;

public class NotificationGenerator {

    MastodonParser mastodonParser;
    ObservableList<RowContent> data = FXCollections.observableArrayList();
    public NotificationGenerator(MastodonParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }

    // 汎用通知項目データクラス
    public static class NotificationContent {
        TimelineGenerator.DataSourceInfo dataSourceInfo;
        String contentText;
        String createdAt;

        public NotificationContent(TimelineGenerator.DataSourceInfo dataSourceInfo,
                                   String contentText,
                                   String createdAt) {
            this.dataSourceInfo = dataSourceInfo;
            this.contentText = contentText;
            this.createdAt = createdAt;
        }
    }

    public static class RowContent {
        public final TimelineGenerator.DataSourceInfo dataSourceInfo;
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty createdAt = new SimpleStringProperty();

        RowContent(TimelineGenerator.DataSourceInfo dataSourceInfo, String contentText, String createdAt){
            this.dataSourceInfo = dataSourceInfo;
            this.contentText.set(contentText);
            this.createdAt.set(createdAt);
        }

        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty createdAtProperty(){ return createdAt; }
    }

    public ObservableList<RowContent> createNotificationContents() {
        var notificationData = mastodonParser.diffNotification();

        for (NotificationContent notification : notificationData) {
            notificationAdd(notification.dataSourceInfo, notification.contentText, notification.createdAt);
        }

        data.sort(Comparator.comparing(notificationContent -> notificationContent.createdAt.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public void notificationAdd(TimelineGenerator.DataSourceInfo dataSourceInfo, String contentText, String createdAt){
        if(data != null){
            data.add(new RowContent(dataSourceInfo, contentText, createdAt));
        }
    }
}
