package services.dao;

import connection.MultipartFormData;
import connection.WebRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static connection.WebRequest.requestGET;

public class MastodonDao {
    public final String mastodonHost;
    public final String accessToken;

    public MastodonDao(String mastodonHost, String accessToken) {
        this.mastodonHost = mastodonHost;
        this.accessToken = accessToken;
    }

    public String reblog(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/" + tootId + "/reblog";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public String addFavorite(String tootId) {
        String url = mastodonHost + "/api/v1/statuses/" + tootId + "/favourite";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String responseBody = WebRequest.requestPOST(url, headers, "");
        return responseBody;
    }

    public String postStatus(String parameterString) {
        String url = mastodonHost + "/api/v1/statuses";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);

        String responseBody = WebRequest.requestPOST(url, headers, parameterString);
        return responseBody;
    }

    public String getHomeTimeline() {
        String url = mastodonHost + "/api/v1/timelines/home";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getLocalTimeline() {
        String url = mastodonHost + "/api/v1/timelines/public?local=true";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getUserTimeline(String userId) {
        String url = mastodonHost + "/api/v1/accounts/" + userId + "/statuses";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String getNotification() {
        String url = mastodonHost + "/api/v1/notifications";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = requestGET(url, headers);
        return responseBody;
    }

    public String uploadMedia(MultipartFormData.FileDto fileDto) {
        String url = mastodonHost + "/api/v1/media";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = MultipartFormData.post(url, headers, new ArrayList<>(Arrays.asList(fileDto)));
        return responseBody;
    }
}
