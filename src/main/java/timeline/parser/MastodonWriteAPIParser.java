package timeline.parser;

import services.MastodonAPI;
import services.Result;

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
        Result response = mastodonAPI.addFavorite(tootId);
        if( response.status == Result.Status.STATUS_FAIL ) return false;
        MastodonTimelineParser.Toot toot = getStatus(response.result);
        return "true".equals(toot.favourited);
    }

    public boolean reblog(String tootId) {
        MastodonAPI mastodonAPI = new MastodonAPI(MASTODON_HOST, MASTODON_TOKEN);
        Result response = mastodonAPI.reblog(tootId);
        if( response.status == Result.Status.STATUS_FAIL ) return false;
        MastodonTimelineParser.Toot toot = getStatus(response.result);
        return "true".equals(toot.reblogged);
    }
}
