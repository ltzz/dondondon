package misc;

import javafx.stage.Stage;

import java.util.TreeMap;

public class TransitionGraph {
    public static void draw(Stage stage, TreeMap<String, Integer> data){
        Graph.draw(stage, data);
    }
}
