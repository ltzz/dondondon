package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import connection.MastodonAPI;
import org.jsoup.Jsoup;
import timeline.NotificationGenerator;
import timeline.TimelineGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MastodonNotificationParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;
    public final String loginUsername;
    private HashMap<String, BufferedImage> iconCache;

    HashSet<String> receivedNotificationIds;

    public MastodonNotificationParser(String mastodonHost, String mastodonToken, String username){
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.loginUsername = username;
        this.receivedNotificationIds = new HashSet<>();
        this.iconCache = new HashMap<>();
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Notification{
        public String id;
        public String type;
        public String created_at;
        public MastodonTimelineParser.Account account;
        public MastodonTimelineParser.Toot status;
    }

    public List<NotificationGenerator.NotificationContent> diffNotification(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        var notifications = getNotificationDto(mastodonAPI.getNotification());
        var filteredNotification = notifications.stream().filter(notification -> !receivedNotificationIds.contains(notification.id)).collect(Collectors.toList());
        var received = notifications.stream().map(notification -> notification.id).collect(Collectors.toSet());
        receivedNotificationIds.addAll(received);
        return toNotificationContent(filteredNotification);
    }

    List<NotificationGenerator.NotificationContent> toNotificationContent(List<Notification> notifications){
        List<NotificationGenerator.NotificationContent> listForGenerator = new ArrayList<>();
        notifications.forEach(notification -> {
            if(notification == null) return;
            var statusText = "";
            var notificationText = "[" + notification.type + "]";
            if(notification.status != null) {
                notificationText = notificationText + " " + Jsoup.parse(notification.status.content).text();
            }

            var avaterURL = "";
            if (MastodonTimelineParser.validateURL(notification.account.avatar_static)) {
                avaterURL = notification.account.avatar_static;
            }

            BufferedImage avatarIcon = null;
            if (MastodonTimelineParser.validateURL(notification.account.avatar_static)) {
                var avatarURL = notification.account.avatar_static;
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

            TimelineGenerator.DataOriginInfo dataOriginInfo = new TimelineGenerator.DataOriginInfo("mastodon", loginUsername, MASTODON_HOST, MASTODON_TOKEN);
            listForGenerator.add(new NotificationGenerator.NotificationContent(dataOriginInfo, notification.id, notification.account.id,
                    notification.account.username, notification.account.display_name, notificationText, notification.created_at, avatarIcon));
        });
        return listForGenerator;
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
}
