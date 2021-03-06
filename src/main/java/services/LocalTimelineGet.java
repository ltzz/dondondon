package services;

public class LocalTimelineGet implements MastodonTimelineSource {
    private final String MASTODON_HOST;
    private final String MASTODON_TOKEN;

    public LocalTimelineGet(String mastodonHost, String mastodonToken) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
    }

    public String get(){
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        return mastodonAPI.getLocalTimeline().result;
    }
}
