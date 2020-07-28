package services;

import javafx.stage.Stage;

import java.util.TreeMap;

public class TransitionGraph {
    public static void draw(String title, Stage stage, TreeMap<String, Integer> data){
        Graph.draw(title,"Transition graph", stage, data);
    }
}
