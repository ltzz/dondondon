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
        public final DataSourceInfo dataSourceInfo;
        public String username;
        public String displayName;
        public String contentText;
        public String date;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUsername;

        public TLContent(DataSourceInfo dataSourceInfo,
                         String username, String displayName,
                         String contentText, String date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername) {
            this.dataSourceInfo = dataSourceInfo;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.date = date;
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.sensitive = sensitive;
            this.reblogOriginalUsername = reblogOriginalUsername;
        }
    }

    public static class RowContent {
        public final DataSourceInfo dataSourceInfo;
        public String contentText;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUserId;
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentTextForDisplay = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        RowContent(TLContent tlContent){
            this.dataSourceInfo = tlContent.dataSourceInfo;
            this.userName.set(tlContent.username + " / " + tlContent.displayName);

            if("false".equals(tlContent.sensitive)){
                this.contentTextForDisplay.set(tlContent.contentText);
            }
            else {
                this.contentTextForDisplay.set("█".repeat(tlContent.contentText.length()*2));
            }

            if(tlContent.reblogOriginalUsername != null){
                this.contentTextForDisplay.set("reblog " + tlContent.reblogOriginalUsername + ": " + tlContent.contentText);
            }
            else {
                this.contentTextForDisplay.set(tlContent.contentText);
            }

            this.contentDate.set(tlContent.date);
            this.favorited = tlContent.favorited;
            this.reblogged = tlContent.reblogged;
            this.sensitive = tlContent.sensitive;
            this.contentText = tlContent.contentText;
            this.reblogOriginalUserId = tlContent.reblogOriginalUsername;
            // TODO
        }
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

