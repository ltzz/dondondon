package connection;

import java.util.HashMap;

import static connection.WebRequest.requestGET;


public class MastodonAPI {
    public final String mastodonHost;
    public final String accessToken;

    public MastodonAPI(String mastodonHost, String accessToken){
        this.mastodonHost = mastodonHost;
        this.accessToken = accessToken;
    }

    public String reblog(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/"+tootId+"/reblog";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);

        var responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public String addFavorite(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/"+tootId+"/favourite";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);

        var responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public void postStatus(String text, String inReplyToId) {
        String url = mastodonHost + "/api/v1/statuses";
        var headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        var parameterString = "status=" + text;
        if( inReplyToId != null ) {
            parameterString = parameterString + "&in_reply_to_id=" + inReplyToId;
        }
        var responseBody = WebRequest.requestPOST(url, headers, parameterString);
        // System.out.println(responseBody);
        // getStatus(responseBody)
        // TODO: ここのレスポンスを見て投稿成功不成功を判断・リストに反映？
    }

    public String getHomeTimeline() {
        String url = mastodonHost + "/api/v1/timelines/home";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getLocalTimeline() {
        String url = mastodonHost + "/api/v1/timelines/public?local=true";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getUserTimeline(String userId) {
        String url = mastodonHost + "/api/v1/accounts/"+userId+"/statuses";
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
