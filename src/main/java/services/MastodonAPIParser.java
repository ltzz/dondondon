package services;

import timeline.parser.MastodonTimelineParser;

public class MastodonAPIParser {
    public static MastodonTimelineParser.UploadMediaResponse upload(String response) {
        return MastodonTimelineParser.getUploadMediaResponse(response);
    }
}
