package timeline.parser;

import javafx.collections.ObservableList;
import timeline.TimelineGenerator;

public interface ITimelineGenerator {
    public ObservableList<TimelineGenerator.RowContent> createRowContents();
    public ObservableList<TimelineGenerator.RowContent> getRowContents();
}
