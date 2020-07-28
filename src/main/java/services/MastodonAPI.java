package services;

import connection.MultipartFormData;
import controller.BottomForm;
import services.dao.MastodonDao;

import java.net.URLEncoder;


public class MastodonAPI {
    public final MastodonDao mastodonDao;

    public MastodonAPI(String mastodonHost, String accessToken) {
        this.mastodonDao = new MastodonDao(mastodonHost, accessToken);
    }

    public String reblog(String tootId) {
        return mastodonDao.reblog(tootId);
    }

    public String addFavorite(String tootId) {
        return mastodonDao.addFavorite(tootId);
    }

    public void postStatus(String postText, BottomForm.FormState formState) {
        String encodedText = "";
        try {
            encodedText = URLEncoder.encode(postText, "UTF-8");
        } catch (Exception e) {
        }
        String parameterString = "status=" + encodedText;
        if (formState.getInReplyToId() != null) {
            parameterString = parameterString + "&in_reply_to_id=" + formState.getInReplyToId();
        }
        if (formState.getImageId() != null) {
            parameterString = parameterString + "&media_ids[]=" + formState.getImageId();
        }
        mastodonDao.postStatus(parameterString);
        // TODO: ここのレスポンスを見て投稿成功不成功を判断・リストに反映？
    }

    public String getHomeTimeline() {
        return mastodonDao.getHomeTimeline();
    }

    public String getLocalTimeline() {
        return mastodonDao.getLocalTimeline();
    }

    public String getUserTimeline(String userId) {
        return mastodonDao.getUserTimeline(userId);
    }

    public String getNotification() {
        return mastodonDao.getNotification();
    }

    public String uploadMedia(MultipartFormData.FileDto fileDto) {
        return mastodonDao.uploadMedia(fileDto);
    }
}
