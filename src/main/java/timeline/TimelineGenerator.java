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
                         String username, String displayName,
                         String contentText, String contentHtml,
                         String contentImageURL,
                         String date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername,
                         String avatarURL) {
            this.dataSourceInfo = dataSourceInfo;
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
        public String contentText;
        public String contentHtml;
        public String contentImageURL;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUserId;
        private ObjectProperty userIcon = new SimpleObjectProperty();
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentTextForDisplay = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        RowContent(TLContent tlContent){
            this.dataSourceInfo = tlContent.dataSourceInfo;

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


            this.userName.set(tlContent.username + " / " + tlContent.displayName);

            if("false".equals(tlContent.sensitive)){
                this.contentTextForDisplay.set(tlContent.contentText);
            }
            else {
                this.contentTextForDisplay.set("█".repeat(tlContent.contentText.length() * 2));
            }

            if(tlContent.reblogOriginalUsername != null){
                this.contentTextForDisplay.set("reblog " + tlContent.reblogOriginalUsername + ": " + tlContent.contentText);
            }
            else {
                this.contentTextForDisplay.set(tlContent.contentText);
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
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextForDisplayProperty(){ return contentTextForDisplay; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    public ObservableList<RowContent> createTootContents(){
        var timelineData = mastodonParser.diffTimeline();

        for (TLContent tldata : timelineData) {
            timelineAdd(tldata);
        }

        data.sort(Comparator.comparing(tootContent -> tootContent.contentDate.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public void timelineAdd(TLContent tlContent){
        if(data != null){
            data.add(new RowContent(tlContent));
        }
    }
}

