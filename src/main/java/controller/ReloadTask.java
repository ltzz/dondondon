package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

public class ReloadTask {

    private Timeline timeline;
    private List<IReload>  generators;

    public ReloadTask(List<IReload> generators){
        this.generators = generators;
    }

    public void start() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(60000),
                        event -> {
                            reload();
                        }
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void manualReload(){
        reload();
    }

    private void reload(){
        for(var generator : generators) {
            generator.reload();
        }
    }

    public void stop() {
        if (timeline == null) return;
        timeline.stop();
    }
}