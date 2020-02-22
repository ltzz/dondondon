package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

public class ReloadTask {

    private Timeline timeline;
    private List<IContentListController>  generators;

    public ReloadTask(List<IContentListController> generators){
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
        for(IContentListController generator : generators) {
            ReloadThread thread = new ReloadThread(generator);
            thread.start();
        }
    }

    public void stop() {
        if (timeline == null) return;
        timeline.stop();
    }

    // TODO: 通信使うやつ全てのスレッド化
    static class ReloadThread extends Thread{
        IContentListController contentListController;
        public ReloadThread(IContentListController contentListController){
            this.contentListController = contentListController;
        }

        public void run(){
            contentListController.reload();
        }
    }
}