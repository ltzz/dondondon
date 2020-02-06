package connection;

import misc.Akan;

import java.util.HashMap;


public class MastodonAPI {

    public void postStatus(String text) {
        String token = Akan.TOKEN;
        String url = Akan.MASTODON_HOST + "/api/v1/statuses";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + token);

        var responseBody = WebRequest.requestPOST(url, headers, "status="+text);
        System.out.println(responseBody);
    }
}
