package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import connection.MastodonAPI;
import org.jsoup.Jsoup;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MastodonParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;

    HashSet<String> receivedStatusIds;
    HashSet<String> receivedNotificationIds;

    public MastodonParser(String mastodonHost, String mastodonToken){
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.receivedStatusIds = new HashSet<>();
        this.receivedNotificationIds = new HashSet<>();
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
        public Object media_attachments;
        public Object mentions;
        public Object tags;
        public String name;
        public Object card;
        public String poll;
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
        @JsonIgnore
        public Object pinned;
        public Object card;
        @JsonIgnore
        public Object poll;
        @JsonIgnore
        public Object application;
        @JsonIgnore
        public Object media_attachments;
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

    public List<TimelineGenerator.TLContent> diffTimeline(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        var toots = getHomeTimelineDto(mastodonAPI.getTimeline());
        var filteredToots = toots.stream().filter(toot -> !receivedStatusIds.contains(toot.id)).collect(Collectors.toList());
        var received = toots.stream().map(toot -> toot.id).collect(Collectors.toSet());
        receivedStatusIds.addAll(received);
        return toTLContent(filteredToots);
    }

    public List<NotificationGenerator.NotificationContent> diffNotification(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        var notifications = getNotificationDto(mastodonAPI.getNotification());
        var filteredNotification = notifications.stream().filter(notification -> !receivedNotificationIds.contains(notification.id)).collect(Collectors.toList());
        var received = notifications.stream().map(notification -> notification.id).collect(Collectors.toSet());
        receivedNotificationIds.addAll(received);
        return toNotificationContent(filteredNotification);
    }

    List<TimelineGenerator.TLContent> toTLContent(List<Toot> toots){
        List<TimelineGenerator.TLContent> listForGenerator = new ArrayList<>();
        toots.forEach(toot -> {
            String text = Jsoup.parse(toot.content).text();
            System.out.println(text);
            String rebloggUser;
            if(toot.reblog == null){
                rebloggUser = null;
            }
            else {
                rebloggUser = toot.reblog.account.username;
            }
            TimelineGenerator.DataSourceInfo dataSourceInfo = new TimelineGenerator.DataSourceInfo("mastodon", MASTODON_HOST, toot.id);
            listForGenerator.add(new TimelineGenerator.TLContent(dataSourceInfo,
                    toot.account.username, toot.account.display_name,
                    text, toot.created_at, toot.favourited, toot.reblogged, toot.sensitive, rebloggUser, toot.account.avatar_static));
        });
        return listForGenerator;
    }

    List<NotificationGenerator.NotificationContent> toNotificationContent(List<Notification> notifications){
        List<NotificationGenerator.NotificationContent> listForGenerator = new ArrayList<>();
        notifications.forEach(notification -> {
            var notificationText = notification.account.username + ": ["+ notification.type + "]";
            TimelineGenerator.DataSourceInfo dataSourceInfo = new TimelineGenerator.DataSourceInfo("mastodon", MASTODON_HOST, notification.id);
            listForGenerator.add(new NotificationGenerator.NotificationContent(dataSourceInfo, notificationText, notification.created_at));
        });
        return listForGenerator;
    }

    List<Toot> getHomeTimelineDto(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Toot> toots = mapper.readValue(json, new TypeReference<List<Toot>>() {});

            return toots;
        }catch (Exception e){
            e.printStackTrace();
        }
        return List.of();
    }

    List<Notification> getNotificationDto(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Notification> notifications = mapper.readValue(json, new TypeReference<List<Notification>>() {});

            return notifications;
        }catch (Exception e){
            e.printStackTrace();
        }
        return List.of();
    }

    Toot getStatus(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Toot toot = mapper.readValue(json, new TypeReference<Toot>() {});

            return toot;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
