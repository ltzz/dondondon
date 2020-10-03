package services;

import timeline.TimelineGenerator;
import timeline.parser.MastodonNotificationParser;
import timeline.parser.MastodonTimelineParser;
import timeline.parser.MastodonWriteAPIParser;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {

    static class TimelineParserWrapper {
        public final String hostname;
        public final MastodonTimelineParser parser;

        public TimelineParserWrapper(String hostname, MastodonTimelineParser parser) {
            this.hostname = hostname;
            this.parser = parser;
        }
    }

    static class NotificationParserWrapper {
        public final String hostname;
        public final MastodonNotificationParser parser;

        public NotificationParserWrapper(String hostname, MastodonNotificationParser parser) {
            this.hostname = hostname;
            this.parser = parser;
        }
    }

    private final HashMap<String, TreeMap<String, TLContent>> tootsAll; // hostname単位で全Tootを持つ
    private final HashMap<String, TreeMap<String, TLContent>> timelines; // タイムライン単位で
    private final HashMap<String, TreeMap<String, NotificationContent>> notificationsAll; // hostname単位で全通知を持つ
    private final HashMap<String, TreeMap<String, NotificationContent>> notifications; // タイムライン単位で
    private final HashMap<String, TimelineParserWrapper> mastodonTimelineParsers;
    private final HashMap<String, NotificationParserWrapper> mastodonNotificationParsers;

    private final List<String> hostNames;
    private final HashMap<String, MastodonWriteAPIParser> writeAPIs; // hostname単位で書き込み用APIを持つ
    private final HashMap<String, MastodonAPI> APIs; // hostname単位でAPIを持つ

    public DataStore(){
        this.tootsAll = new HashMap<>();
        this.timelines = new HashMap<>();
        this.notificationsAll = new HashMap<>();
        this.notifications = new HashMap<>();
        this.mastodonTimelineParsers = new HashMap<>();
        this.mastodonNotificationParsers = new HashMap<>();
        this.hostNames = new ArrayList<>();
        this.writeAPIs = new HashMap<>();
        this.APIs = new HashMap<>();
    }

    public void putTimelineParser(String key, String hostname, MastodonTimelineParser parser){
        this.mastodonTimelineParsers.put(key, new TimelineParserWrapper(hostname, parser));
    }

    public void putNotificationParser(String key, String hostname, MastodonNotificationParser parser){
        this.mastodonNotificationParsers.put(key, new NotificationParserWrapper(hostname, parser));
    }

    public void putWriteParser(String hostname, MastodonWriteAPIParser parser){
        this.writeAPIs.put(hostname, parser);
    }

    public Optional<MastodonWriteAPIParser> getWriteParser(String host){
        return writeAPIs.containsKey(host) ?
                Optional.of(writeAPIs.get(host)) : Optional.empty();
    }

    public void putAPI(String hostname, MastodonAPI parser){
        this.APIs.put(hostname, parser);
    }

    public Optional<MastodonAPI> getAPI(String host){
        return APIs.containsKey(host) ?
                Optional.of(APIs.get(host)) : Optional.empty();
    }


    public void addHostName(String hostName){
        this.hostNames.add(hostName);
    }

    public List<String> getHostNames(){
        return new ArrayList<>(hostNames);
    }

    public Optional<TimelineParserWrapper> getTimelineParser(String key){
        return mastodonTimelineParsers.containsKey(key) ?
                Optional.of(mastodonTimelineParsers.get(key)) : Optional.empty();
    }

    public Optional<NotificationParserWrapper> getNotificationParser(String key){
        return mastodonNotificationParsers.containsKey(key) ?
                Optional.of(mastodonNotificationParsers.get(key)) : Optional.empty();
    }

    public Optional<TLContent> getToot(String hostName, String statusId){
        TreeMap<String, TLContent> hostToots = tootsAll.getOrDefault(hostName, new TreeMap<>());
        return hostToots.containsKey(statusId) ?
                Optional.of(hostToots.get(statusId)) : Optional.empty();
    }

    public Optional<NotificationContent> getNotification(String hostName, String notificationId){
        TreeMap<String, NotificationContent> hostNotifications = notificationsAll.getOrDefault(hostName, new TreeMap<>());
        return hostNotifications.containsKey(notificationId) ?
                Optional.of(hostNotifications.get(notificationId)) : Optional.empty();
    }

    public List<TLContent> getTLContentList(String key){
        Optional<TimelineParserWrapper> parser = getTimelineParser(key);
        if(!parser.isPresent()){
            return List.of();
        }
        List<TLContent> timelineData = parser.get().parser.getTimeline();
        String hostName = parser.get().hostname;
        if(!timelines.containsKey(key)){
            timelines.put(key, new TreeMap<>());
        }
        if(!tootsAll.containsKey(hostName)){
            tootsAll.put(hostName, new TreeMap<>());
        }
        TreeMap<String, TLContent> targetMap = timelines.get(key);
        TreeMap<String, TLContent> targetMapToot = tootsAll.get(hostName);

        for (DataStore.TLContent tldata : timelineData) {
            targetMap.put(tldata.id, tldata); // FIXME: 上書きなので投稿削除とかの時の挙動が謎
            targetMapToot.put(tldata.id, tldata);
        }

        List<TLContent> fetchedList = new ArrayList<>(targetMap.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK

        return fetchedList;
    }

    public List<NotificationContent> getNotificationContentList(String key){
        Optional<NotificationParserWrapper> parser = getNotificationParser(key);
        if(!parser.isPresent()){
            return List.of();
        }
        List<NotificationContent> notificationData = parser.get().parser.diffNotification();
        String hostName = parser.get().hostname;
        if(!notifications.containsKey(key)){
            notifications.put(key, new TreeMap<>());
        }
        if(!notificationsAll.containsKey(hostName)){
            notificationsAll.put(hostName, new TreeMap<>());
        }
        TreeMap<String, NotificationContent> targetMap = notifications.get(key);
        TreeMap<String, NotificationContent> targetMapAll = notificationsAll.get(hostName);

        for (NotificationContent notification : notificationData) {
            targetMap.put(notification.id, notification);
            targetMapAll.put(notification.id, notification);
        }

        List<NotificationContent> fetchedList = new ArrayList<>(targetMap.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK

        return fetchedList;
    }


    public static class TLContent {
        public final TimelineGenerator.DataOriginInfo dataOriginInfo;
        public final MastodonWriteAPIParser writeActionApi;
        public final String id;
        public final String userId;
        public final String acct;
        public final String username;
        public final String displayName;
        public final String contentText;
        public final String contentHtml;
        public final List<TimelineGenerator.EmojiData> emojis;
        public final List<String> contentImageURL;
        public final String url;
        public final String applicationName;
        public final String applicationWebSite;
        public final Date date;
        public final Date reblogDate;
        public final String favorited;
        public final String reblogged;
        public final String spoilerText;
        public final String sensitive;
        public final String reblogUsername;
        public final BufferedImage avatarIcon;
        public final HashMap<String, Object> instanceSpecificData;

        public TLContent(TimelineGenerator.DataOriginInfo dataOriginInfo,
                         MastodonWriteAPIParser writeActionApi,
                         String id,
                         String userId, String acct,
                         String username, String displayName,
                         String contentText, String contentHtml,
                         List<TimelineGenerator.EmojiData> emojis,
                         List<String> contentImageURL,
                         String url,
                         String applicationName, String applicationWebSite,
                         Date date,
                         Date reblogDate,
                         String favorited, String reblogged,
                         String spoilerText, String sensitive,
                         String reblogUsername,
                         BufferedImage avatarIcon,
                         HashMap<String, Object> instanceSpecificData) {
            this.dataOriginInfo = dataOriginInfo;
            this.writeActionApi = writeActionApi;
            this.id = id;
            this.userId = userId;
            this.acct = acct;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.contentHtml = contentHtml;
            this.emojis = emojis;
            this.contentImageURL = contentImageURL;
            this.url = url;
            this.applicationName = applicationName;
            this.applicationWebSite = applicationWebSite;
            this.date = date;
            this.reblogDate = reblogDate;
            this.favorited = favorited;
            this.reblogged = reblogged;
            this.spoilerText = spoilerText;
            this.sensitive = sensitive;
            this.reblogUsername = reblogUsername;
            this.avatarIcon = avatarIcon;
            this.instanceSpecificData = instanceSpecificData;
        }
    }

    public static class NotificationContent {
        public final TimelineGenerator.DataOriginInfo dataOriginInfo;
        public final String id;
        public final Optional<String> statusId;
        public final String userId;
        public final String acct;
        public final String username;
        public final String displayName;
        public final String contentText;
        public final Date createdAt;
        public final BufferedImage avatarIcon;
        public final HashMap<String, Object> instanceSpecificData;

        public NotificationContent(TimelineGenerator.DataOriginInfo dataOriginInfo,
                                   String id,
                                   Optional<String> statusId,
                                   String userId,
                                   String acct,
                                   String username, String displayName,
                                   String contentText,
                                   Date createdAt,
                                   BufferedImage avatarIcon,
                                   HashMap<String, Object> instanceSpecificData) {
            this.dataOriginInfo = dataOriginInfo;
            this.id = id;
            this.statusId = statusId;
            this.userId = userId;
            this.acct = acct;
            this.username = username;
            this.displayName = displayName;
            this.contentText = contentText;
            this.createdAt = createdAt;
            this.avatarIcon = avatarIcon;
            this.instanceSpecificData = instanceSpecificData;
        }
    }
}
