package timeline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import services.Common;
import services.DataStore;
import timeline.parser.ITimelineGenerator;
import timeline.parser.MastodonWriteAPIParser;

import java.util.*;
import java.util.stream.Collectors;

import static services.date.DateParseService.dateToGraphString;
import static services.date.DateParseService.dateToJapaneseString;

public class TimelineGenerator implements ITimelineGenerator {

    private TreeMap<String, RowContent> fetchedContents;
    private String generatorName;
    private DataStore dataStore;

    public TimelineGenerator(String generatorName, DataStore dataStore)  {
        this.fetchedContents = new TreeMap<String, RowContent>();
        this.generatorName = generatorName;
        this.dataStore = dataStore;
    }

    public static class DataOriginInfo {
        public String serverType;
        public String hostname;
        public String username;
        private String token;

        public DataOriginInfo(String serverType, String hostname, String username, String token) {
            this.serverType = serverType;
            this.hostname = hostname;
            this.username = username;
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    public static class EmojiData {
        private String imageURL;
        private String shortCode;

        public EmojiData(String shortCode, String imageURL) {
            if (Common.validateURL(imageURL)) {
                this.imageURL = imageURL;
            } else {
                this.imageURL = "";
            }
            this.shortCode = shortCode;
        }

        public String getShortCode() {
            return shortCode;
        }

        public String toImageTag() {
            return "<img src='" + this.imageURL + "' class='emoji' />";
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
        public String spoilerText;
        public String reblogUsername;
        public String url;
        public String visibility;
        public String applicationName;
        public String applicationWebSite;
        public Date date;
        public Date reblogOriginDate;
        private ObjectProperty<ImageView> userIcon = new SimpleObjectProperty<ImageView>();
        public StringProperty userNameForColumn = new SimpleStringProperty();
        public StringProperty contentTextForColumn = new SimpleStringProperty();
        public StringProperty dateForColumn = new SimpleStringProperty();

        RowContent(DataStore.TLContent tlContent) {
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
            } catch (Exception e) {

            }

            this.userNameForColumn.set(tlContent.username + " / " + tlContent.displayName);
            StringBuffer stringBuffer = new StringBuffer();

            this.visibility = (String) tlContent.instanceSpecificData.get("visibility");

            if (((String) tlContent.instanceSpecificData.get("visibility")).equals("private")) {
                stringBuffer.append("[非公開投稿]");
            }

            if ((tlContent.instanceSpecificData.get("poll")) != null) {
                stringBuffer.append("[投票]");
            }

            if (tlContent.reblogUsername != null) {
                stringBuffer.append("[reblog by " + tlContent.reblogUsername + "]");
            }
            if (tlContent.contentImageURL != null && !tlContent.contentImageURL.isEmpty()) {
                stringBuffer.append("[画像]");
            }

            if (!tlContent.spoilerText.isEmpty()) {
                stringBuffer.append(tlContent.spoilerText);
            }

            if ("false".equals(tlContent.sensitive)) {
                stringBuffer.append(tlContent.contentText);
            } else {
                for (int i = 0; i < tlContent.contentText.length() * 2; ++i) {
                    stringBuffer.append("█");
                }
            }

            this.contentTextForColumn.set(stringBuffer.toString());

            this.contentHtml = applyEmoji(tlContent.contentHtml, tlContent.emojis);
            this.url = tlContent.url;
            this.applicationName = tlContent.applicationName;
            this.applicationWebSite = tlContent.applicationWebSite;
            this.date = tlContent.date;
            this.reblogOriginDate = tlContent.reblogDate;

            this.dateForColumn.set(dateToJapaneseString(tlContent.date));
            this.favorited = tlContent.favorited;
            this.reblogged = tlContent.reblogged;
            this.spoilerText = tlContent.spoilerText;
            this.sensitive = tlContent.sensitive;
            this.contentImageURL = tlContent.contentImageURL;
            this.contentText = tlContent.contentText;
            this.reblogUsername = tlContent.reblogUsername;
            // TODO
        }

        public ObjectProperty<ImageView> userIconProperty() {
            return userIcon;
        }

        public StringProperty userNameProperty() {
            return userNameForColumn;
        }

        public StringProperty contentTextProperty() {
            return contentTextForColumn;
        }

        public StringProperty dateProperty() {
            return dateForColumn;
        }
    }

    private static String applyEmoji(String contentHtml, List<EmojiData> emojis) {
        String retContentHtml = contentHtml;
        for (EmojiData emojiData : emojis) {
            String targetShortCode = ":" + emojiData.getShortCode() + ":";
            retContentHtml = retContentHtml.replaceAll(targetShortCode, emojiData.toImageTag());
        }
        return retContentHtml;
    }

    public String getGeneratorName() {
        return this.generatorName;
    }

    public ObservableList<RowContent> createRowContents() {
        List<DataStore.TLContent> fetchedList = dataStore.getTLContentList(generatorName);
        List<RowContent> rowContentList = fetchedList.stream().map(RowContent::new).collect(Collectors.toList());
        rowContentList.forEach(item -> fetchedContents.put(item.id, item));
        return FXCollections.observableArrayList(rowContentList);
    }

    public ObservableList<RowContent> getRowContents() {
        List<TimelineGenerator.RowContent> fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }

    public int getNumberOfContent() {
        return fetchedContents.size();
    }

    public TreeMap<String, Integer> getNumberOfContentByHours() {
        TreeMap<String, Integer> graphData = new TreeMap<String, Integer>();
        for (TimelineGenerator.RowContent fetchedContent : fetchedContents.values()) {
            String dateLabel = dateToGraphString(fetchedContent.date);
            if (!graphData.containsKey(dateLabel)) {
                graphData.put(dateLabel, 0);
            }
            graphData.put(dateLabel, graphData.get(dateLabel) + 1);
        }
        return graphData;
    }
}