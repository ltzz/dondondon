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
        public String username;
        public String contentText;
        public String date;
        public TLContent(String username, String contentText, String date){
            this.username = username;
            this.contentText = contentText;
            this.date = date;
        }
    }

    public static class TootContent{
        public StringProperty userName = new SimpleStringProperty();
        public StringProperty contentText = new SimpleStringProperty();
        public StringProperty contentDate = new SimpleStringProperty();

        TootContent(String userName, String contentText, String contentDate){
            this.userName.set(userName);
            this.contentText.set(contentText);
            this.contentDate.set(contentDate); // TODO
        }
        public StringProperty userNameProperty(){ return userName; }
        public StringProperty contentTextProperty(){ return contentText; }
        public StringProperty contentDateProperty(){ return contentDate; }
    }

    MastodonParser mastodonParser;
    ObservableList<TootContent> data = FXCollections.observableArrayList();

    public TimelineGenerator(MastodonParser mastodonParser){
        this.mastodonParser = mastodonParser;
    }

    public ObservableList<TootContent> createTootContents(){
        var timelineData = mastodonParser.diffTimeline();

        for (TLContent tldata : timelineData) {
            timelineAdd(tldata.username, tldata.contentText, tldata.date);
        }
        data.sort(Comparator.comparing(tootContent -> tootContent.contentDate.get()));
        Collections.reverse(data); // FIXME: 同時刻の投稿が実行するたびに逆順になる
        return data;
    }

    public void timelineAdd(String username, String contentText, String contentDate){
        if(data != null){
            data.add(new TootContent(username, contentText, contentDate));
        }
    }
}

