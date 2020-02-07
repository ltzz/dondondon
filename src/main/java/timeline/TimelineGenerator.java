package timeline;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timeline.parser.MastodonParser;

import java.util.Collections;
import java.util.Comparator;

public class TimelineGenerator {

    // 汎用タイムライン項目データクラス
    public static class TLContent{
        public String tootId;
        public String username;
        public String contentText;
        public String date;
        public String favorited;
        public String reblogged;
        public String sensitive;

        public TLContent(String tootId, String username, String contentText, String date,
                         String favorited, String reblogged, String sensitive) {
            this.tootId = tootId;
            this.username = username;
            this.contentText = contentText;
            this.date = date;
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
        }
    }

    public static class RowContent {
        public String contentText;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public StringProperty tootId = new SimpleStringProperty();
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentTextForDisplay = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        RowContent(String tootId, String userName, String contentText, String contentDate, String favorited, String reblogged, String sensitive){
            this.tootId.set(tootId);
            this.userName.set(userName);
            if("false".equals(sensitive)){
                this.contentTextForDisplay.set(contentText);
            }
            else {
                this.contentTextForDisplay.set("█".repeat(contentText.length()*2));
            }
            this.contentDate.set(contentDate);
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
            this.contentText = contentText;
            // TODO
        }
        public StringProperty tootIdProperty(){ return tootId; }
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextForDisplayProperty(){ return contentTextForDisplay; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    MastodonParser mastodonParser;
    ObservableList<RowContent> data = FXCollections.observableArrayList();

    public TimelineGenerator(MastodonParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }

    public ObservableList<RowContent> createTootContents(){
        var timelineData = mastodonParser.diffTimeline();

        for (TLContent tldata : timelineData) {
            timelineAdd(tldata.tootId, tldata.username, tldata.contentText, tldata.date, tldata.favorited, tldata.reblogged, tldata.sensitive);
        }
        data.sort(Comparator.comparing(tootContent -> tootContent.contentDate.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public void timelineAdd(String tootId, String username, String contentText, String contentDate, String favorited, String reblogged, String sensitive){
        if(data != null){
            data.add(new RowContent(tootId,username, contentText, contentDate, favorited, reblogged, sensitive));
        }
    }
}

