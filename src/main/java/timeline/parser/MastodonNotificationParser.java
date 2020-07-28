package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import services.MastodonAPI;
import misc.ImageCommons;
import org.jsoup.Jsoup;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MastodonNotificationParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    public final String loginUsername;
    private ConcurrentHashMap<String, BufferedImage> iconCache;

    HashSet<String> receivedNotificationIds;

    public MastodonNotificationParser(String mastodonHost, String mastodonToken, String username, ConcurrentHashMap<String, BufferedImage> iconCache) {
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
        List<Notification> notifications = getNotificationDto(mastodonAPI.getNotification());
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

            BufferedImage avatarIcon = null;
            if (MastodonTimelineParser.validateURL(notification.account.avatar_static)) {
                String avatarURL = notification.account.avatar_static;
                try {
                    // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                    if (iconCache.containsKey(avatarURL)) {
                        avatarIcon = iconCache.get(avatarURL);
                    } else {
                        byte[] buffer = ImageCommons.readImageAsByte(new URL(avatarURL));
                        if (buffer != null) {
                            avatarIcon = ImageCommons.readImage(buffer);
                            iconCache.put(avatarURL, avatarIcon);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            Date createdAt = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                createdAt = format.parse(notification.created_at);
            } catch (Exception e) {
                e.printStackTrace();
            }


            TimelineGenerator.DataOriginInfo dataOriginInfo = new TimelineGenerator.DataOriginInfo("mastodon", loginUsername, MASTODON_HOST, MASTODON_TOKEN);
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
