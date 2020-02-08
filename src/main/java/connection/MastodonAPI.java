package connection;

import misc.Akan;

import java.util.HashMap;

import static connection.WebRequest.requestGET;


public class MastodonAPI {
    String mastodonHost;
    String accessToken;

    public MastodonAPI(String mastodonHost, String accessToken){
        this.mastodonHost = mastodonHost;
        this.accessToken = accessToken;
    }

    public void addFavorite(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/"+tootId+"/favourite";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);

        var responseBody = WebRequest.requestPOST(url, headers, "");
        System.out.println(responseBody);
    }

    public void postStatus(String text) {
        String url = mastodonHost + "/api/v1/statuses";
        var headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        var responseBody = WebRequest.requestPOST(url, headers, "status=" + text);
        // System.out.println(responseBody);
        // getStatus(responseBody)
        // TODO: ここのレスポンスを見てお気に入り状態を表示に反映
    }

    public String getTimeline() {
        String url = mastodonHost + "/api/v1/timelines/home";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getNotification() {
        String url = mastodonHost + "/api/v1/notifications";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }
}
