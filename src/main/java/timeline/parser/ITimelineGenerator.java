package timeline.parser;

import javafx.collections.ObservableList;
import timeline.TimelineGenerator;

import java.util.TreeMap;

public interface ITimelineGenerator {
    public ObservableList<TimelineGenerator.RowContent> createRowContents();
    public ObservableList<TimelineGenerator.RowContent> getRowContents();
    public int getNumberOfContent();
    public TreeMap<String, Integer> getNumberOfContentByHours();
}
