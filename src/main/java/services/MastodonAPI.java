package services;

import utils.http.MultipartFormData;
import controller.BottomForm;
import services.dao.MastodonDao;

import java.net.URLEncoder;


public class MastodonAPI {
    public final MastodonDao mastodonDao;

    public MastodonAPI(String mastodonHost, String accessToken) {
        this.mastodonDao = new MastodonDao(mastodonHost, accessToken);
    }

    public Result reblog(String tootId) {
        return mastodonDao.reblog(tootId);
    }

    public Result addFavorite(String tootId) {
        return mastodonDao.addFavorite(tootId);
    }

    public Result postStatus(String postText, BottomForm.FormState formState, int publishingLevelIndex) {
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
        if (!MastodonConstant.publishingLevels.get(publishingLevelIndex).equals("default")) {
            parameterString = parameterString + "&visibility=" + MastodonConstant.publishingLevels.get(publishingLevelIndex);
        }
        return mastodonDao.postStatus(parameterString);
    }

    public Result getHomeTimeline() {
        return mastodonDao.getHomeTimeline();
    }

    public Result getLocalTimeline() {
        return mastodonDao.getLocalTimeline();
    }

    public Result getUserTimeline(String userId) {
        return mastodonDao.getUserTimeline(userId);
    }

    public Result getNotification() {
        return mastodonDao.getNotification();
    }

    public Result uploadMedia(MultipartFormData.FileDto fileDto) {
        return mastodonDao.uploadMedia(fileDto);
    }
}
