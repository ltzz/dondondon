package timeline.parser;

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

public class MastodonNotificationParser {

    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;

    HashSet<String> receivedNotificationIds;

    public MastodonNotificationParser(String mastodonHost, String mastodonToken){
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.receivedNotificationIds = new HashSet<>();
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
            var notificationText = notification.account.username + ": ["+ notification.type + "] " + Jsoup.parse(notification.status.content).text();

            var avaterURL = "";
            if (MastodonTimelineParser.validateURL(notification.account.avatar_static)) {
                avaterURL = notification.account.avatar_static;
            }

            TimelineGenerator.DataSourceInfo dataSourceInfo = new TimelineGenerator.DataSourceInfo("mastodon", MASTODON_HOST, notification.id);
            listForGenerator.add(new NotificationGenerator.NotificationContent(dataSourceInfo, notificationText, notification.created_at, avaterURL));
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
