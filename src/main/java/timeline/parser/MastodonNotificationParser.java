package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import services.date.DateParseService;
import services.IconCacheService;
import services.MastodonAPI;
import org.jsoup.Jsoup;
import services.DataStore;
import timeline.TimelineGenerator;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MastodonNotificationParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    public final String loginUsername;
    private ConcurrentHashMap<String, BufferedImage> iconCache;

    HashSet<String> receivedNotificationIds;

    public MastodonNotificationParser(String mastodonHost, String mastodonToken, String username,
                                      ConcurrentHashMap<String, BufferedImage> iconCache) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.loginUsername = username;
        this.receivedNotificationIds = new HashSet<>();
        this.iconCache = iconCache;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notification {
        public String id;
        public String type;
        public String created_at;
        public MastodonTimelineParser.Account account;
        public MastodonTimelineParser.Toot status;
    }

    public List<DataStore.NotificationContent> diffNotification() {
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        List<Notification> notifications = getNotificationDto(mastodonAPI.getNotification().result);
        List<Notification> filteredNotification = notifications.stream()
                .filter(notification -> !receivedNotificationIds.contains(notification.id)).collect(Collectors.toList());
        Set<String> received = notifications.stream().map(notification -> notification.id).collect(Collectors.toSet());
        receivedNotificationIds.addAll(received);
        return toNotificationContent(filteredNotification);
    }

    List<DataStore.NotificationContent> toNotificationContent(List<Notification> notifications) {
        List<DataStore.NotificationContent> listForGenerator = new ArrayList<>();
        notifications.forEach(notification -> {
            if (notification == null) return;
            HashMap<String, Object> mastodonSpecificData = new HashMap<>();
            String notificationText = "[" + notification.type + "]";
            Optional<String> statusId = Optional.empty();
            if (notification.status != null) {
                notificationText = notificationText + " " + Jsoup.parse(notification.status.content).text();
                mastodonSpecificData.put("visibility", notification.status.visibility);
                mastodonSpecificData.put("notification_type", notification.type);
                statusId = Optional.of(notification.status.id);
            }

            BufferedImage avatarIcon = IconCacheService.addIcon(iconCache, notification.account.avatar_static);

            Date createdAt = DateParseService.parse(notification.created_at);

            TimelineGenerator.DataOriginInfo dataOriginInfo =
                    new TimelineGenerator.DataOriginInfo("mastodon", loginUsername, MASTODON_HOST, MASTODON_TOKEN);
            listForGenerator.add(new DataStore.NotificationContent(dataOriginInfo, notification.id, statusId, notification.account.id,
                    notification.account.acct,
                    notification.account.username, notification.account.display_name, notificationText, createdAt, avatarIcon,
                    mastodonSpecificData));
        });
        return listForGenerator;
    }

    List<Notification> getNotificationDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Notification> notifications = mapper.readValue(json, new TypeReference<List<Notification>>() {
            });

            return notifications;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList();
    }
}
