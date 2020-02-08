package timeline;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timeline.parser.MastodonParser;

import java.util.Collections;
import java.util.Comparator;

public class TimelineGenerator {

    MastodonParser mastodonParser;
    ObservableList<RowContent> data = FXCollections.observableArrayList();

    public TimelineGenerator(MastodonParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }


    // 汎用タイムライン項目データクラス
    public static class TLContent{
        public String serverType;
        public String hostname;
        public String tootId;
        public String username;
        public String contentText;
        public String date;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUsername;

        public TLContent(String serverType, String hostname, String tootId,
                         String username, String contentText, String date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername) {
            this.serverType = serverType;
            this.hostname = hostname;
            this.tootId = tootId;
            this.username = username;
            this.contentText = contentText;
            this.date = date;
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
            this.reblogOriginalUsername = reblogOriginalUsername;
        }
    }

    public static class RowContent {
        public String contentText;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUserId;
        public String serverType;
        public String hostname;
        public String tootId;
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentTextForDisplay = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        RowContent(String serverType, String hostname, String tootId,
                   String userName, String contentText, String contentDate, String favorited, String reblogged, String sensitive, String reblogOriginalUserId){
            this.serverType = serverType;
            this.hostname = hostname;
            this.tootId = tootId;
            this.userName.set(userName);

            if("false".equals(sensitive)){
                this.contentTextForDisplay.set(contentText);
            }
            else {
                this.contentTextForDisplay.set("█".repeat(contentText.length()*2));
            }

            if(reblogOriginalUserId != null){
                this.contentTextForDisplay.set("reblog " + reblogOriginalUserId + ": " + contentText);
            }
            else {
                this.contentTextForDisplay.set(contentText);
            }

            this.contentDate.set(contentDate);
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
            this.contentText = contentText;
            this.reblogOriginalUserId = reblogOriginalUserId;
            // TODO
        }
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextForDisplayProperty(){ return contentTextForDisplay; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    public ObservableList<RowContent> createTootContents(){
        var timelineData = mastodonParser.diffTimeline();

        for (TLContent tldata : timelineData) {
            timelineAdd(tldata.serverType, tldata.hostname, tldata.tootId, tldata.username, tldata.contentText, tldata.date,
                    tldata.favorited, tldata.reblogged, tldata.sensitive,
                    tldata.reblogOriginalUsername);
        }
        data.sort(Comparator.comparing(tootContent -> tootContent.contentDate.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public void timelineAdd(String serverType, String hostname, String tootId,
                            String username, String contentText, String contentDate, String favorited, String reblogged, String sensitive, String reblogOriginalUsername){
        if(data != null){
            data.add(new RowContent(serverType, hostname, tootId,
                    username, contentText, contentDate,
                    favorited, reblogged, sensitive, reblogOriginalUsername));
        }
    }
}

