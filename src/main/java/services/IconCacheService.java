package services;


import utils.ImageCommons;
import timeline.parser.MastodonTimelineParser;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class IconCacheService {
    public static BufferedImage addIcon(ConcurrentHashMap<String, BufferedImage> iconCache, String url){
        BufferedImage avatarIcon = null;
        if (MastodonTimelineParser.validateURL(url)) {
            String avatarURL = url;
            try {
                // TODO: この実装セキュリティ的に大丈夫かどうか詳しい人に聞く
                if (iconCache.containsKey(avatarURL)) {
                    avatarIcon = iconCache.get(avatarURL);
                } else {
                    byte[] buffer = ImageCommons.readImageAsByte(new URL(avatarURL));
                    if (buffer != null) {
                        avatarIcon = ImageCommons.readImage(buffer);
                        iconCache.put(avatarURL, avatarIcon);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return avatarIcon;
    }
}
