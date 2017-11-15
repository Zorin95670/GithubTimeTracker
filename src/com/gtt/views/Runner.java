package com.gtt.views;

import com.gtt.core.Apps;
import com.gtt.views.connection.ConnectionView;

import javafx.application.Application;
import javafx.stage.Stage;

public class Runner extends Application {

    private Stage primaryStage;
    private DefaultView defaultView;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Github Time Tracker");

        defaultView = new ConnectionView(this, primaryStage);
    }

    public void changeView(final DefaultView view) {
        defaultView = view;
    }
    
    @Override
    public void stop() throws Exception {
    	Apps.getScheduled().shutdownNow();
    	super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
