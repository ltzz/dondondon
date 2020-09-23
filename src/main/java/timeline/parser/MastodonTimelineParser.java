package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import services.DateParseService;
import services.IconCacheService;
import timeline.TimelineGenerator;
import services.MastodonTimelineSource;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MastodonTimelineParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    public final String loginUsername;
    private ConcurrentHashMap<String, BufferedImage> iconCache;

    private MastodonTimelineSource endPoint;

    public MastodonTimelineParser(String mastodonHost, String mastodonToken,
                                  MastodonTimelineSource endPoint, String username,
                                  ConcurrentHashMap<String, BufferedImage> iconCache) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.loginUsername = username;
        this.endPoint = endPoint;
        this.iconCache = iconCache;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Media {
        public String id;
        public String type;
        public String url;
        public String preview_url;
        public Object meta;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Application {
        public String name;
        public String website;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Toot {
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
        public Toot reblog;
        public List<Media> media_attachments;
        public Object mentions;
        public Object tags;
        public String name;
        public Object card;
        public Application application;
        public List<Emoji> emojis;
        public Object poll;
        @JsonIgnore
        public Object pinned;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Emoji {
        public String shortcode;
        public String url;
        public String static_url;
        public Boolean visible_in_picker;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notification {
        public String id;
        public String type;
        public String created_at;
        public Account account;
        public Toot status;
    }

    public List<TimelineGenerator.TLContent> getTimeline() {
        List<Toot> toots = getTimelineDto(endPoint.get());
        return toTLContent(toots);
    }

    private static String getApplicationName(Application application) {
        if (application != null) {
            return application.name;
        } else {
            return "";
        }
    }

    private static String getApplicationWebSite(Application application) {
        if (application != null) {
            return application.website;
        } else {
            return "";
        }
    }

    private static Toot getTootOrReblog(Toot toot) {
        if (toot.reblog == null) {
            return toot;
        } else {
            return toot.reblog;
        }
    }

    List<TimelineGenerator.TLContent> toTLContent(List<Toot> toots) {
        List<TimelineGenerator.TLContent> listForGenerator = new ArrayList<>();
        toots.forEach(toot -> {
            String text = Jsoup.parse(toot.content).text();
            String htmltext = Jsoup.clean(toot.content, "", Whitelist.basic(), new Document.OutputSettings().prettyPrint(false));
            System.out.println(text);

            Toot tootEntity = getTootOrReblog(toot);

            String usernameReblogBy;
            if (toot.reblog == null) {
                usernameReblogBy = null;
            } else {
                usernameReblogBy = toot.account.username;
            }

            List<String> imagesURL = new ArrayList<>();

            // TODO: typeを使って動画であることを画面上で明示
            if (toot.media_attachments.size() > 0) {
                for (Media media_attachment : toot.media_attachments) {
                    if (validateURL(media_attachment.preview_url)) {
                        imagesURL.add(media_attachment.preview_url);
                    }
                }
            } else if (toot.reblog != null && toot.reblog.media_attachments.size() > 0) {
                // TODO: なんかReblogの時の画像はこっちにあるようだ（本文はtoot.contentにあるのに) よく分からんので調べる
                for (Media media_attachment : toot.reblog.media_attachments) {
                    if (validateURL(media_attachment.preview_url)) {
                        imagesURL.add(media_attachment.preview_url);
                    }
                }
            }

            BufferedImage avatarIcon = IconCacheService.addIcon(iconCache, tootEntity.account.avatar_static);

            Date createdAt = DateParseService.parse(toot.created_at);
            Date reblogCreatedAt = toot.reblog != null ? DateParseService.parse(toot.reblog.created_at) : null;

            HashMap<String, Object> mastodonSpecificData = new HashMap<String, Object>();
            mastodonSpecificData.put("visibility", tootEntity.visibility);
            mastodonSpecificData.put("poll", tootEntity.poll);

            MastodonWriteAPIParser mastodonWriteAPIParser = new MastodonWriteAPIParser(MASTODON_HOST, MASTODON_TOKEN);

            TimelineGenerator.DataOriginInfo dataOriginInfo =
                    new TimelineGenerator.DataOriginInfo("mastodon", MASTODON_HOST, loginUsername, MASTODON_TOKEN);
            listForGenerator.add(new TimelineGenerator.TLContent(dataOriginInfo,
                    mastodonWriteAPIParser,
                    toot.id,
                    tootEntity.account.id, tootEntity.account.acct,
                    tootEntity.account.username, tootEntity.account.display_name,
                    text, htmltext,
                    tootEntity.emojis.stream()
                            .map(emoji -> new TimelineGenerator.EmojiData(emoji.shortcode, emoji.static_url))
                            .collect(Collectors.toList()),
                    imagesURL,
                    tootEntity.url,
                    getApplicationName(toot.application), getApplicationWebSite(toot.application),
                    createdAt, reblogCreatedAt, toot.favourited, toot.reblogged,
                    tootEntity.spoiler_text, tootEntity.sensitive,
                    usernameReblogBy, avatarIcon,
                    mastodonSpecificData
            ));
        });
        return listForGenerator;
    }

    static List<Toot> getTimelineDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Toot> toots = mapper.readValue(json, new TypeReference<List<Toot>>() {
            });

            return toots;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList();
    }

    static Toot getStatus(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Toot toot = mapper.readValue(json, new TypeReference<Toot>() {
            });

            return toot;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadMediaResponse {
        public String id;
        public String type;
        public String url;
        public String preview_url;
        public String remote_url;
        public String text_url;
    }


    public static UploadMediaResponse getUploadMediaResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UploadMediaResponse dto = mapper.readValue(json, new TypeReference<UploadMediaResponse>() {
            });

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validateURL(String url) {
        // URLとしてあり得る記号のみ許可する
        // TODO: 詳しい人にこれで安全か聞く
        if(url == null) return false;
        return Pattern.compile("^https?://[a-zA-Z0-9/:%#&~=_!'\\$\\?\\(\\)\\.\\+\\*\\-]+$").matcher(url).matches();
    }
}
