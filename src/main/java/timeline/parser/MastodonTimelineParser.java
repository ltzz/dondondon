package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import timeline.TimelineGenerator;
import timeline.parser.timelineEndPoint.MastodonTimelineEndPoint;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MastodonTimelineParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    private HashMap<String, BufferedImage> iconCache;

    private MastodonTimelineEndPoint endPoint;

    public MastodonTimelineParser(String mastodonHost, String mastodonToken, MastodonTimelineEndPoint endPoint){
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.endPoint = endPoint;
        this.iconCache = new HashMap<>();
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Account{
        public String id;
        public String username;
        public String acct;
        public String display_name;
        public String locked;
        public String bot;
        public String created_at;
        public String note;
        public String url;
        public String avatar;
        public String avatar_static;
        public String header;
        public String header_static;
        public int followers_count;
        public int following_count;
        public int statuses_count;
        public String last_status_at;
        @JsonIgnore
        public List<Object> emojis;
        @JsonIgnore
        public List<Object> fields;

    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Media {
        public String id;
        public String type;
        public String url;
        public String preview_url;
        public Object meta;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Reblog {
        public String id;
        public String created_at;
        public String in_reply_to_id;
        public String in_reply_to_account_id;
        public String sensitive;
        public String spoiler_text;
        public String visibility;
        public String language;
        public String uri;
        public String url;
        public String replies_count;
        public String reblogs_count;
        public String favourites_count;
        public String favourited;
        public String reblogged;
        public String muted;
        public String content;
        public String reblog;
        public Account account;
        public List<Media> media_attachments;
        public Object mentions;
        public Object tags;
        public String name;
        public Object card;
        public Object poll;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Toot{
        public String id;
        public String created_at;
        public String in_reply_to_id;
        public String in_reply_to_account_id;
        public String sensitive;
        public String spoiler_text;
        public String visibility;
        public String language;
        public String uri;
        public String url;
        public String replies_count;
        public String reblogs_count;
        public String favourites_count;
        public String favourited;
        public String reblogged;
        public String muted;
        public String content;
        public Account account;
        public Reblog reblog;
        public List<Media> media_attachments;
        @JsonIgnore
        public Object pinned;
        public Object card;
        @JsonIgnore
        public Object poll;
        @JsonIgnore
        public Object application;
        @JsonIgnore
        public List<Object> mentions;
        @JsonIgnore
        public List<Object> tags;
        @JsonIgnore
        public List<Object> emojis;
    }


    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Notification{
        public String id;
        public String type;
        public String created_at;
        public Account account;
        public Toot status;
    }

    public List<TimelineGenerator.TLContent> getTimeline(){
        var toots = getTimelineDto(endPoint.get());
        return toTLContent(toots);
    }

    List<TimelineGenerator.TLContent> toTLContent(List<Toot> toots){
        List<TimelineGenerator.TLContent> listForGenerator = new ArrayList<>();
        toots.forEach(toot -> {
            String text = Jsoup.parse(toot.content).text();
            var htmltext = Jsoup.clean(toot.content, "", Whitelist.basic(), new Document.OutputSettings().prettyPrint(false));
            System.out.println(text);
            String rebloggUser;
            if(toot.reblog == null){
                rebloggUser = null;
            }
            else {
                rebloggUser = toot.reblog.account.username;
            }

            // TODO: 複数画像の対応
            var imageURL = "";

            if(toot.media_attachments.size() > 0) {
                if (validateURL(toot.media_attachments.get(0).preview_url)) {
                    imageURL = toot.media_attachments.get(0).preview_url;
                }
            }
            else if(toot.reblog != null && toot.reblog.media_attachments.size() > 0) {
                // TODO: なんかReblogの時の画像はこっちにあるようだ（本文はtoot.contentにあるのに) よく分からんので調べる
                if (validateURL(toot.reblog.media_attachments.get(0).preview_url)) {
                    imageURL = toot.reblog.media_attachments.get(0).preview_url;
                }
            }

            BufferedImage avatarIcon = null;
            if (validateURL(toot.account.avatar_static)) {
                var avatarURL = toot.account.avatar_static;
                try {
                    // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                    if(iconCache.containsKey(avatarURL)){
                        avatarIcon = iconCache.get(avatarURL);
                    }
                    else {
                        avatarIcon = ImageIO.read(new URL(avatarURL));
                        iconCache.put(avatarURL, avatarIcon);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            TimelineGenerator.DataSourceInfo dataSourceInfo = new TimelineGenerator.DataSourceInfo("mastodon", MASTODON_HOST);
            listForGenerator.add(new TimelineGenerator.TLContent(dataSourceInfo,
                    toot.id,
                    toot.account.id, toot.account.acct,
                    toot.account.username, toot.account.display_name,
                    text, htmltext, imageURL,
                    toot.created_at, toot.favourited, toot.reblogged, toot.sensitive, rebloggUser, avatarIcon));
        });
        return listForGenerator;
    }

    static List<Toot> getTimelineDto(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Toot> toots = mapper.readValue(json, new TypeReference<List<Toot>>() {});

            return toots;
        }catch (Exception e){
            e.printStackTrace();
        }
        return List.of();
    }

    static Toot getStatus(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Toot toot = mapper.readValue(json, new TypeReference<Toot>() {});

            return toot;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateURL(String url){
        // URLとしてあり得る記号のみ許可する
        // TODO: 詳しい人にこれで安全か聞く
        return Pattern.compile("^https?://[a-zA-Z0-9/:%#&~=_!'\\$\\?\\(\\)\\.\\+\\*\\-]+$").matcher(url).matches();
    }
}
