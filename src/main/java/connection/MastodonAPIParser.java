package connection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import timeline.parser.MastodonTimelineParser;

import java.util.List;

public class MastodonAPIParser {
    public static MastodonTimelineParser.UploadMediaResponse upload(String response){
        return MastodonTimelineParser.getUploadMediaResponse(response);
    }
}
