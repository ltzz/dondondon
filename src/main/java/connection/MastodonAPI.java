package connection;

import controller.Controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public String addFavorite(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/"+tootId+"/favourite";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public void postStatus(String postText, Controller.FormState formState) {
        String url = mastodonHost + "/api/v1/statuses";
        HashMap<String,String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String encodedText = "";
        try {
            encodedText = URLEncoder.encode(postText, "UTF-8");
        }catch(Exception e){
        }
        String parameterString = "status=" + encodedText;
        if( formState.getInReplyToId() != null ) {
            parameterString = parameterString + "&in_reply_to_id=" + formState.getInReplyToId();
        }
        if( formState.getImageId() != null ) {
            parameterString = parameterString + "&media_ids[]=" + formState.getImageId();
        }
        String responseBody = WebRequest.requestPOST(url, headers, parameterString);
        // System.out.println(responseBody);
        // getStatus(responseBody)
        // TODO: ここのレスポンスを見て投稿成功不成功を判断・リストに反映？
    }

    public String getHomeTimeline() {
        String url = mastodonHost + "/api/v1/timelines/home";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getLocalTimeline() {
        String url = mastodonHost + "/api/v1/timelines/public?local=true";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getUserTimeline(String userId) {
        String url = mastodonHost + "/api/v1/accounts/"+userId+"/statuses";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getNotification() {
        String url = mastodonHost + "/api/v1/notifications";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String uploadMedia(MultipartFormData.FileDto fileDto) {
        String url = mastodonHost + "/api/v1/media";
        HashMap<String,String> headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = MultipartFormData.post(url, headers, new ArrayList<>(Arrays.asList(fileDto)));
        return responseBody;
    }
}
