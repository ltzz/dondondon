package timeline;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timeline.parser.ITimelineGenerator;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MixTimelineGenerator implements ITimelineGenerator {
    private String generatorName;

    public static class MixTimelineId implements Comparable<MixTimelineId> {
        private String id;
        private Date date;
        private String host;

        public MixTimelineId(String id, Date date, String host){
            this.id = id;
            this.host = host;
            this.date = date;
        }

        @Override
        public int compareTo(MixTimelineId mixTimelineId) {
            if(this.date.after(mixTimelineId.date)){
                return 1;
            } else if(this.date.before(mixTimelineId.date)) {
                return -1;
            }
            else {
                if(this.host.compareTo(mixTimelineId.host) != 0){
                    return this.host.compareTo(mixTimelineId.host);
                }
                else {
                    return this.id.compareTo(mixTimelineId.id);
                }
            }
        }
    }

    TimelineGenerator timelineGenerator1;
    TimelineGenerator timelineGenerator2;
    private TreeMap<MixTimelineId, TimelineGenerator.RowContent> fetchedContents;
    public MixTimelineGenerator(TimelineGenerator timelineGenerator1, TimelineGenerator timelineGenerator2){
        this.fetchedContents = new TreeMap<MixTimelineId, TimelineGenerator.RowContent>();
        this.timelineGenerator1 = timelineGenerator1;
        this.timelineGenerator2 = timelineGenerator2;
    }

    public void setGeneratorName(String name){
        this.generatorName = name;
    }

    public String getGeneratorName(){
        return this.generatorName;
    }

    public ObservableList<TimelineGenerator.RowContent> createRowContents(){

        ObservableList<TimelineGenerator.RowContent> rowContents1 = timelineGenerator1.createRowContents();
        ObservableList<TimelineGenerator.RowContent> rowContents2 = timelineGenerator2.createRowContents();

        for (TimelineGenerator.RowContent rowContent : rowContents1) {
            fetchedContents.put(new MixTimelineId(rowContent.id, rowContent.date, rowContent.dataOriginInfo.hostname), rowContent);
        }
        for (TimelineGenerator.RowContent rowContent : rowContents2) {
            fetchedContents.put(new MixTimelineId(rowContent.id, rowContent.date, rowContent.dataOriginInfo.hostname), rowContent);
        }

        List<TimelineGenerator.RowContent> fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);

        return FXCollections.observableArrayList(fetchedList);
    }

    public ObservableList<TimelineGenerator.RowContent> getRowContents(){
        List<TimelineGenerator.RowContent> fetchedList = new ArrayList<>(fetchedContents.values());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }

    public int getNumberOfContent(){
        return fetchedContents.size();
    }

    public TreeMap<String, Integer> getNumberOfContentByHours(){
        TreeMap<String, Integer> graphData = new TreeMap<String, Integer>();
        for( TimelineGenerator.RowContent fetchedContent : fetchedContents.values()){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:00");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());

            String dateLabel = simpleDateFormat.format(fetchedContent.date);
            if( !graphData.containsKey(dateLabel) ){
                graphData.put(dateLabel, 0);
            }
            graphData.put(dateLabel, graphData.get(dateLabel)+1);
        }
        return graphData;
    }
}
