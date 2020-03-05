package timeline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import timeline.parser.ITimelineGenerator;
import timeline.parser.MastodonTimelineParser;
import timeline.parser.MastodonWriteAPIParser;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimelineGenerator implements ITimelineGenerator {

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
        MastodonWriteAPIParser writeActionApi;
        String id;
        String userId;
        String acct;
        String username;
        String displayName;
        String contentText;
        String contentHtml;
        List<String> contentImageURL;
        String url;
        String applicationName;
        String applicationWebSite;
        Date date;
        String favorited;
        String reblogged;
        String sensitive;
        String reblogOriginalUsername;
        BufferedImage avatarIcon;

        public TLContent(DataOriginInfo dataOriginInfo,
                         MastodonWriteAPIParser writeActionApi,
                         String id,
                         String userId, String acct,
                         String username, String displayName,
                         String contentText, String contentHtml,
                         List<String> contentImageURL,
                         String url,
                         String applicationName, String applicationWebSite,
                         Date date,
                         String favorited, String reblogged, String sensitive,
                         String reblogOriginalUsername,
                         BufferedImage avatarIcon) {
            this.dataOriginInfo = dataOriginInfo;
            this.writeActionApi = writeActionApi;
            this.id = id;
            this.userId = userId;
            this.acct = acct;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.contentHtml = contentHtml;
            this.contentImageURL = contentImageURL;
            this.url = url;
            this.applicationName = applicationName;
            this.applicationWebSite = applicationWebSite;
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
        public MastodonWriteAPIParser writeActionApi;
        public String id;
        public String userId;
        public String userName;
        public String acct;
        public String contentText;
        public String contentHtml;
        public List<String> contentImageURL;
        public String favorited;
        public String reblogged;
        public String sensitive;
        public String reblogOriginalUserId;
        public String url;
        public String applicationName;
        public String applicationWebSite;
        public Date date;
        private ObjectProperty<ImageView> userIcon = new SimpleObjectProperty<ImageView>();
        public StringProperty userNameForColumn = new SimpleStringProperty();
        public StringProperty contentTextForColumn = new SimpleStringProperty();
        public StringProperty dateForColumn = new SimpleStringProperty();

        RowContent(TLContent tlContent){
            this.dataOriginInfo = tlContent.dataOriginInfo;
            this.writeActionApi = tlContent.writeActionApi;
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
            StringBuffer stringBuffer = new StringBuffer();

            if(tlContent.reblogOriginalUsername != null){
                stringBuffer.append("reblog " + tlContent.reblogOriginalUsername + ": ");
            }

            if("false".equals(tlContent.sensitive)){
                stringBuffer.append(tlContent.contentText);
            }
            else {
                for(int i=0; i < tlContent.contentText.length() * 2; ++i){
                    stringBuffer.append("█");
                }
            }


            if(tlContent.contentImageURL != null && !tlContent.contentImageURL.isEmpty()){
                stringBuffer.append("[画像]");
            }
            this.contentTextForColumn.set(stringBuffer.toString());

            this.contentHtml = tlContent.contentHtml;
            this.url = tlContent.url;
            this.applicationName = tlContent.applicationName;
            this.applicationWebSite = tlContent.applicationWebSite;
            this.date = tlContent.date;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            this.dateForColumn.set(simpleDateFormat.format(tlContent.date));
            this.favorited = tlContent.favorited;
            this.reblogged = tlContent.reblogged;
            this.sensitive = tlContent.sensitive;
            this.contentImageURL = tlContent.contentImageURL;
            this.contentText = tlContent.contentText;
            this.reblogOriginalUserId = tlContent.reblogOriginalUsername;
            // TODO
        }

        public ObjectProperty<ImageView> userIconProperty(){ return userIcon; }
        public StringProperty userNameProperty(){ return userNameForColumn; }
        public StringProperty contentTextProperty(){ return contentTextForColumn; }
        public StringProperty dateProperty(){ return dateForColumn; }
    }

    public ObservableList<RowContent> createRowContents(){

        List<TimelineGenerator.TLContent> timelineData = mastodonParser.getTimeline();

        for (TLContent tldata : timelineData) {
            fetchedContents.put(tldata.id, new RowContent(tldata)); // FIXME: 上書きなので投稿削除とかの時の挙動が謎
        }
        List<TimelineGenerator.RowContent> fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);

        return FXCollections.observableArrayList(fetchedList);
    }

    public ObservableList<RowContent> getRowContents(){
        List<TimelineGenerator.RowContent>  fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }

    public int getNumberOfContent(){
        return fetchedContents.size();
    }
}

