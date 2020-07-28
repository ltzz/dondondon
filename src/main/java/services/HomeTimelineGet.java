package services;

public class HomeTimelineGet implements MastodonTimelineEndPoint {
    private final String MASTODON_HOST;
    private final String MASTODON_TOKEN;

    public HomeTimelineGet(String mastodonHost, String mastodonToken) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
    }

    public String get(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        return mastodonAPI.getHomeTimeline();
    }
}
