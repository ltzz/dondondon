package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import connection.WebRequest;
import misc.Akan;
import org.jsoup.Jsoup;
import timeline.TimelineGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static connection.WebRequest.requestGET;

public class MastodonParser {

    HashSet<String> receivedTootIds;

    public MastodonParser(){
        this.receivedTootIds = new HashSet<>();
    }

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
        public Object pinned;
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

    public List<TimelineGenerator.TLContent> diffTimeline(){
        var toots = getHomeTimelineDto(getTimeline());
        var filteredToots = toots.stream().filter(toot -> !receivedTootIds.contains(toot.id)).collect(Collectors.toList());
        var received = toots.stream().map(toot -> toot.id).collect(Collectors.toSet());
        receivedTootIds.addAll(received);
        return tootToTLContent(filteredToots);
    }

    static List<TimelineGenerator.TLContent> tootToTLContent(List<Toot> toots){
        List<TimelineGenerator.TLContent> listForTL = new ArrayList<>();
        toots.forEach(toot -> {
            //String text = toot.content;
            String text = Jsoup.parse(toot.content).text();
            System.out.println(text);
            listForTL.add(new TimelineGenerator.TLContent(toot.id, toot.account.display_name, text, toot.created_at));
        });
        return listForTL;
    }

    private String getTimeline() {
        String token = Akan.TOKEN;
        String url = Akan.MASTODON_HOST + "/api/v1/timelines/home";
        var headers = new HashMap<String,String>();
        headers.put("Authorization", "Bearer " + token);
        var responseBody = requestGET(url, headers);
        return responseBody;
    }

    List<Toot> getHomeTimelineDto(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Toot> toots = mapper.readValue(json, new TypeReference<List<Toot>>() {});

            return toots;
        }catch (Exception e){
            e.printStackTrace();
        }
        return List.of();
    }
}
