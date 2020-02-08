package timeline.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import connection.MastodonAPI;
import misc.Akan;
import org.jsoup.Jsoup;
import timeline.TimelineGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MastodonParser {

    HashSet<String> receivedStatusIds;

    public MastodonParser(){
        this.receivedStatusIds = new HashSet<>();
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
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

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Media {

    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Reblog {
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
        public String reblog;
        public Account account;
        public Object media_attachments;
        public Object mentions;
        public Object tags;
        public String name;
        public Object card;
        public String poll;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
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
        public Reblog reblog;
        @JsonIgnore
        public Object pinned;
        public Object card;
        @JsonIgnore
        public Object poll;
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
        MastodonAPI mastodonAPI = new MastodonAPI(Akan.MASTODON_HOST, Akan.TOKEN);
        var toots = getHomeTimelineDto(mastodonAPI.getTimeline());
        var filteredToots = toots.stream().filter(toot -> !receivedStatusIds.contains(toot.id)).collect(Collectors.toList());
        var received = toots.stream().map(toot -> toot.id).collect(Collectors.toSet());
        receivedStatusIds.addAll(received);
        return tootToTLContent(filteredToots);
    }

    static List<TimelineGenerator.TLContent> tootToTLContent(List<Toot> toots){
        List<TimelineGenerator.TLContent> listForTL = new ArrayList<>();
        toots.forEach(toot -> {
            String text = Jsoup.parse(toot.content).text();
            System.out.println(text);
            String rebloggUser;
            if(toot.reblog == null){
                rebloggUser = null;
            }
            else {
                rebloggUser = toot.reblog.account.username;
            }
            TimelineGenerator.DataSourceInfo dataSourceInfo = new TimelineGenerator.DataSourceInfo("mastodon", Akan.MASTODON_HOST, toot.id);
            listForTL.add(new TimelineGenerator.TLContent(dataSourceInfo,
                    toot.account.display_name, text, toot.created_at, toot.favourited, toot.reblogged, toot.sensitive, rebloggUser));
        });
        return listForTL;
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

    Toot getStatus(String json){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Toot toot = mapper.readValue(json, new TypeReference<Toot>() {});

            return toot;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
