import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.util.Date;
import java.util.TimerTask;

public class ReloadTask {

    private Timeline timeline;
    private TableView<TimelineGenerator.TootContent> tableView;
    private TimelineGenerator timelineGenerator;

    public ReloadTask(TableView<TimelineGenerator.TootContent> tableView, TimelineGenerator timelineGenerator){
        this.tableView = tableView;
        this.timelineGenerator = timelineGenerator;
    }

    public void start() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(60000),
                        event -> {
                            ObservableList<TimelineGenerator.TootContent> tootContents = timelineGenerator.createTootContents(); // TODO:
                            tableView.setItems(tootContents);
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