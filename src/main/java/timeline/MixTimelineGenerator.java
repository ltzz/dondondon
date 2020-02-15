package timeline;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import timeline.parser.ITimelineGenerator;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MixTimelineGenerator implements ITimelineGenerator {

    public static class MixTimelineId implements Comparable<MixTimelineId> {
        private String id;
        private Date date;
        private String host;

        public MixTimelineId(String id, String dateStr, String host){
            this.id = id;
            this.host = host;
            this.date = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                this.date = format.parse(dateStr);
            }
            catch (Exception e){
                e.printStackTrace();
            }
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


    public ObservableList<TimelineGenerator.RowContent> createRowContents(){

        ObservableList<TimelineGenerator.RowContent> rowContents1 = timelineGenerator1.createRowContents();
        ObservableList<TimelineGenerator.RowContent> rowContents2 = timelineGenerator2.createRowContents();

        for (var rowContent : rowContents1) {
            fetchedContents.put(new MixTimelineId(rowContent.id, rowContent.contentDate.get(), rowContent.dataOriginInfo.hostname), rowContent);
        }
        for (var rowContent : rowContents2) {
            fetchedContents.put(new MixTimelineId(rowContent.id, rowContent.contentDate.get(), rowContent.dataOriginInfo.hostname), rowContent);
        }

        var fetchedList = fetchedContents.values().stream().collect(Collectors.toList());
        Collections.reverse(fetchedList);

        return FXCollections.observableArrayList(fetchedList);
    }

    public ObservableList<TimelineGenerator.RowContent> getRowContents(){
        var fetchedList = fetchedContents.values().stream().collect(Collectors.toList());
        Collections.reverse(fetchedList);  // MastodonではIDの上位48bitは時刻なのでソートに使ってOK
        return FXCollections.observableArrayList(fetchedList);
    }
}
