package timeline.parser;

import connection.MastodonAPI;

import static timeline.parser.MastodonTimelineParser.getStatus;

public class MastodonWriteAPIParser {
    public final String MASTODON_HOST;
    public final String MASTODON_TOKEN;

    public MastodonWriteAPIParser(String mastodonHost, String mastodonToken) {
        this.MASTODON_HOST = mastodonHost;
        this.MASTODON_TOKEN = mastodonToken;
    }

    public boolean addFavorite(String tootId) {
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        String response = mastodonAPI.addFavorite(tootId);
        MastodonTimelineParser.Toot toot = getStatus(response);
        return "true".equals(toot.favourited);
    }

    public boolean reblog(String tootId) {
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        String response = mastodonAPI.reblog(tootId);
        MastodonTimelineParser.Toot toot = getStatus(response);
        return "true".equals(toot.reblogged);
    }
}
