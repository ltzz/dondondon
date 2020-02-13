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

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TimelineGenerator {

    MastodonTimelineParser mastodonParser;
    private TreeMap<String, RowContent> fetchedContents;

    public TimelineGenerator(MastodonTimelineParser mastodonParser){
        this.mastodonParser = mastodonParser;
        this.fetchedContents = new TreeMap<String, RowContent>();
    }

    public static class DataOriginInfo {
        public String serverType;
        public String hostname;
        public String username;
        private String token;
        public DataOriginInfo(String serverType, String hostname, String username, String token){
            this.serverType = serverType;
            this.hostname = hostname;
            this.username = username;
            this.token = token;
        }
        public String getToken(){
            return token;
        }
    }

    // 汎用タイムライン項目データクラス
    public static class TLContent{
        final DataOriginInfo dataOriginInfo;
        String id;
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
        BufferedImage avatarIcon;

        public TLContent(DataOriginInfo dataOriginInfo,
                         String id,
                         String userId, String acct,
                         String username, String displayName,
                         String contentText, String contentHtml,
                         String contentImageURL,
                         String date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername,
                         BufferedImage avatarIcon) {
            this.dataOriginInfo = dataOriginInfo;
            this.id = id;
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
            this.avatarIcon = avatarIcon;
        }
    }

    public static class RowContent {
        public DataOriginInfo dataOriginInfo;
        public String id;
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
            this.dataOriginInfo = tlContent.dataOriginInfo;
            this.id = tlContent.id;
            this.userId = tlContent.userId;
            this.acct = tlContent.acct;
            this.userName = tlContent.username;

            try {
                ImageView iconView = new ImageView(SwingFXUtils.toFXImage(tlContent.avatarIcon, null));
                iconView.setFitWidth(20);
                iconView.setFitHeight(20);
                this.userIcon.set(iconView);
            }catch (Exception e){

            }

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

        var timelineData = mastodonParser.getTimeline();

        for (TLContent tldata : timelineData) {
            fetchedContents.put(tldata.id, new RowContent(tldata)); // FIXME: 上書きなので投稿削除とかの時の挙動が謎
        }
        var fetchedList = fetchedContents.values().stream().collect(Collectors.toList());
        Collections.reverse(fetchedList);

        return FXCollections.observableArrayList(fetchedList);
    }

    public ObservableList<RowContent> getRowContents(){
        var fetchedList = fetchedContents.values().stream().collect(Collectors.toList());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }
}

