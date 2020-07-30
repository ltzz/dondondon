package services;

public class UserTimelineGet implements MastodonTimelineSource {
    private final String MASTODON_HOST;
    private final String MASTODON_TOKEN;
    private final String userId;

    public UserTimelineGet(String mastodonHost, String mastodonToken, String userId) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
        this.userId = userId;
    }

    public String get(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        return mastodonAPI.getUserTimeline(userId).result;
    }
}
