package misc;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import timeline.TimelineGenerator;

public class ReloadTask {

    private Timeline timeline;
    private TableView<TimelineGenerator.RowContent> tableView;
    private TimelineGenerator timelineGenerator;

    public ReloadTask(TableView<TimelineGenerator.RowContent> tableView, TimelineGenerator timelineGenerator){
        this.tableView = tableView;
        this.timelineGenerator = timelineGenerator;
    }

    public void start() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(60000),
                        event -> {
                            ObservableList<TimelineGenerator.RowContent> rowContents = timelineGenerator.createTootContents(); // TODO:
                            tableView.setItems(rowContents);
                        }
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stop() {
        if (timeline == null) return;
        timeline.stop();
    }
}