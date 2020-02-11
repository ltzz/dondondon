package timeline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import timeline.parser.MastodonTimelineParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

public class TimelineGenerator {

    MastodonTimelineParser mastodonParser;
    ObservableList<RowContent> data = FXCollections.observableArrayList();

    public TimelineGenerator(MastodonTimelineParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }

    public static class DataSourceInfo{
        public String serverType;
        public String hostname;
        public String statusId;
        public DataSourceInfo(String serverType, String hostname, String statusId){
            this.serverType = serverType;
            this.hostname = hostname;
            this.statusId = statusId;
        }
    }

    // 汎用タイムライン項目データクラス
    public static class TLContent{
        final DataSourceInfo dataSourceInfo;
        String userId;
        String acct;
        String username;
        String displayName;
        String contentText;
        String contentHtml;
        String contentImageURL;
        String date;
        String favorited;
        String reblogged;
        String sensitive;
        String reblogOriginalUsername;
        String avatarURL;

        public TLContent(DataSourceInfo dataSourceInfo,
                         String userId, String acct,
                         String username, String displayName,
                         String contentText, String contentHtml,
                         String contentImageURL,
                         String date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername,
                         String avatarURL) {
            this.dataSourceInfo = dataSourceInfo;
            this.userId = userId;
            this.acct = acct;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.contentHtml = contentHtml;
            this.contentImageURL = contentImageURL;
            this.date = date;
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
            this.reblogOriginalUsername = reblogOriginalUsername;
            this.avatarURL = avatarURL;
        }
    }

    public static class RowContent {
        public DataSourceInfo dataSourceInfo;
        public String userId;
        public String userName;
        public String acct;
        public String contentText;
        public String contentHtml;
        public String contentImageURL;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUserId;
        private ObjectProperty userIcon = new SimpleObjectProperty();
        public StringProperty userNameForColumn = new SimpleStringProperty();
        public StringProperty contentTextForColumn = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        RowContent(TLContent tlContent){
            this.dataSourceInfo = tlContent.dataSourceInfo;
            this.userId = tlContent.userId;
            this.acct = tlContent.acct;

                    BufferedImage icon = null;
            try {
                // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                icon = ImageIO.read(new URL(tlContent.avatarURL));
                ImageView iconView = new ImageView(SwingFXUtils.toFXImage(icon, null));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                this.userIcon.set(iconView);
            }catch (Exception e){

            }

            this.userName = tlContent.username;
            this.userNameForColumn.set(tlContent.username + " / " + tlContent.displayName);

            if("false".equals(tlContent.sensitive)){
                this.contentTextForColumn.set(tlContent.contentText);
            }
            else {
                this.contentTextForColumn.set("█".repeat(tlContent.contentText.length() * 2));
            }

            if(tlContent.reblogOriginalUsername != null){
                this.contentTextForColumn.set("reblog " + tlContent.reblogOriginalUsername + ": " + tlContent.contentText);
            }
            else {
                this.contentTextForColumn.set(tlContent.contentText);
            }

            this.contentHtml = tlContent.contentHtml;

            this.contentDate.set(tlContent.date);
            this.favorited = tlContent.favorited;
            this.reblogged = tlContent.reblogged;
            this.sensitive = tlContent.sensitive;
            this.contentImageURL = tlContent.contentImageURL;
            this.contentText = tlContent.contentText;
            this.reblogOriginalUserId = tlContent.reblogOriginalUsername;
            // TODO
        }

        public ObjectProperty userIconProperty(){ return userIcon; }
        public StringProperty userNameProperty(){ return userNameForColumn; }
        public StringProperty contentTextProperty(){ return contentTextForColumn; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    public ObservableList<RowContent> createRowContents(){
        var timelineData = mastodonParser.diffTimeline();

        for (TLContent tldata : timelineData) {
            timelineAdd(tldata);
        }

        data.sort(Comparator.comparing(tootContent -> tootContent.contentDate.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public ObservableList<RowContent> getRowContents(){
        return data;
    }

   public void timelineAdd(TLContent tlContent){
        if(data != null){
            data.add(new RowContent(tlContent));
        }
    }
}

