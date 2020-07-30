package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import services.DateParseService;
import services.IconCacheService;
import services.MastodonAPI;
import org.jsoup.Jsoup;
import timeline.NotificationGenerator;
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

    public List<NotificationGenerator.NotificationContent> diffNotification() {
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        List<Notification> notifications = getNotificationDto(mastodonAPI.getNotification().result);
        List<Notification> filteredNotification = notifications.stream()
                .filter(notification -> !receivedNotificationIds.contains(notification.id)).collect(Collectors.toList());
        Set<String> received = notifications.stream().map(notification -> notification.id).collect(Collectors.toSet());
        receivedNotificationIds.addAll(received);
        return toNotificationContent(filteredNotification);
    }

    List<NotificationGenerator.NotificationContent> toNotificationContent(List<Notification> notifications) {
        List<NotificationGenerator.NotificationContent> listForGenerator = new ArrayList<>();
        notifications.forEach(notification -> {
            if (notification == null) return;
            String notificationText = "[" + notification.type + "]";
            if (notification.status != null) {
                notificationText = notificationText + " " + Jsoup.parse(notification.status.content).text();
            }

            BufferedImage avatarIcon = IconCacheService.addIcon(iconCache, notification.account.avatar_static);

            Date createdAt = DateParseService.parse(notification.created_at);

            TimelineGenerator.DataOriginInfo dataOriginInfo =
                    new TimelineGenerator.DataOriginInfo("mastodon", loginUsername, MASTODON_HOST, MASTODON_TOKEN);
            listForGenerator.add(new NotificationGenerator.NotificationContent(dataOriginInfo, notification.id, notification.account.id,
                    notification.account.username, notification.account.display_name, notificationText, createdAt, avatarIcon));
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
