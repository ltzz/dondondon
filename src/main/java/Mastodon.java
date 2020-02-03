import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Mastodon {

    public static class Account{
        public String id;
        public String username;
        public String acct;
        public String display_name;
        public String locked;
        public String bot;
        public String created_at;
        public String note;
        public String url;
        public String avatar;
        public String avatar_static;
        public String header;
        public String header_static;
        public int followers_count;
        public int following_count;
        public int statuses_count;
        public String last_status_at;
        @JsonIgnore
        public List<Object> emojis;
        @JsonIgnore
        public List<Object> fields;

    }


    public static class Media {

    }


    public static class Toot{
        public String id;
        public String created_at;
        public String in_reply_to_id;
        public String in_reply_to_account_id;
        public String sensitive;
        public String spoiler_text;
        public String visibility;
        public String language;
        public String uri;
        public String url;
        public String replies_count;
        public String reblogs_count;
        public String favourites_count;
        public String favourited;
        public String reblogged;
        public String muted;
        public String content;
        public Account account;
        @JsonIgnore
        public Object card;
        @JsonIgnore
        public Object poll;
        @JsonIgnore
        public Object reblog;
        @JsonIgnore
        public Object application;
        @JsonIgnore
        public Object media_attachments;
        @JsonIgnore
        public List<Object> mentions;
        @JsonIgnore
        public List<Object> tags;
        @JsonIgnore
        public List<Object> emojis;

    }

    static void parseTimeline(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Toot> toots = mapper.readValue(json, new TypeReference<List<Toot>>() {});
            toots.forEach(toot -> System.out.println(toot.content));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
