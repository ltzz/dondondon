package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import misc.ImageCommons;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import timeline.TimelineGenerator;
import timeline.parser.timelineEndPoint.MastodonTimelineEndPoint;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class MastodonTimelineParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    public final String loginUsername;
    private ConcurrentHashMap<String, BufferedImage> iconCache;

    private MastodonTimelineEndPoint endPoint;

    public MastodonTimelineParser(String mastodonHost, String mastodonToken, MastodonTimelineEndPoint endPoint, String username, ConcurrentHashMap<String, BufferedImage> iconCache){
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.loginUsername = username;
        this.endPoint = endPoint;
        this.iconCache = iconCache;
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
    public static class Application {
        public String name;
        public String website;
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
        public Application application;
        @JsonIgnore
        public Object pinned;
        public Object card;
        @JsonIgnore
        public Object poll;
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
        List<Toot> toots = getTimelineDto(endPoint.get());
        return toTLContent(toots);
    }

    private String getApplicationName(Application application){
        if(application != null){
            return application.name;
        }
        else {
            return "";
        }
    }

    private String getApplicationWebSite(Application application){
        if(application != null){
            return application.website;
        }
        else {
            return "";
        }
    }

    List<TimelineGenerator.TLContent> toTLContent(List<Toot> toots){
        List<TimelineGenerator.TLContent> listForGenerator = new ArrayList<>();
        toots.forEach(toot -> {
            String text = Jsoup.parse(toot.content).text();
            String htmltext = Jsoup.clean(toot.content, "", Whitelist.basic(), new Document.OutputSettings().prettyPrint(false));
            System.out.println(text);
            String rebloggUser;
            if(toot.reblog == null){
                rebloggUser = null;
            }
            else {
                rebloggUser = toot.reblog.account.username;
            }

            List<String> imagesURL = new ArrayList<>();

            if(toot.media_attachments.size() > 0) {
                for( Media media_attachment : toot.media_attachments ){
                    if (validateURL(media_attachment.preview_url)) {
                        imagesURL.add(media_attachment.preview_url);
                    }
                }
            }
            else if(toot.reblog != null && toot.reblog.media_attachments.size() > 0) {
                // TODO: なんかReblogの時の画像はこっちにあるようだ（本文はtoot.contentにあるのに) よく分からんので調べる
                for( Media media_attachment : toot.reblog.media_attachments ){
                    if (validateURL(media_attachment.preview_url)) {
                        imagesURL.add(media_attachment.preview_url);
                    }
                }
            }

            final String avatarStatic;
            BufferedImage avatarIcon = null;
            if(toot.reblog != null){
                avatarStatic = toot.reblog.account.avatar_static;
            }
            else {
                avatarStatic = toot.account.avatar_static;
            }
            if (validateURL(avatarStatic)) {
                String avatarURL = avatarStatic;
                try {
                    // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                    if(iconCache.containsKey(avatarURL)){
                        avatarIcon = iconCache.get(avatarURL);
                    }
                    else {
                        byte[] buffer = ImageCommons.readImageAsByte(new URL(avatarURL));
                        if(buffer != null) {
                            avatarIcon = ImageCommons.readImage(buffer);
                            iconCache.put(avatarURL, avatarIcon);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            Date createdAt = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                createdAt = format.parse(toot.created_at);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            TimelineGenerator.DataOriginInfo dataOriginInfo = new TimelineGenerator.DataOriginInfo("mastodon", MASTODON_HOST, loginUsername, MASTODON_TOKEN);
            listForGenerator.add(new TimelineGenerator.TLContent(dataOriginInfo,
                    new MastodonWriteAPIParser(MASTODON_HOST, MASTODON_TOKEN),
                    toot.id,
                    toot.account.id, toot.account.acct,
                    toot.account.username, toot.account.display_name,
                    text, htmltext, imagesURL,
                    toot.url,
                    getApplicationName(toot.application), getApplicationWebSite(toot.application),
                    createdAt, toot.favourited, toot.reblogged,
                    toot.spoiler_text, toot.sensitive,
                    rebloggUser, avatarIcon));
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
        return Arrays.asList();
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
