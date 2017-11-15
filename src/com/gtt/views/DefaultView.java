package com.gtt.views;

import javafx.stage.Stage;

public abstract class DefaultView {

    private Stage primaryStage;
    private Runner runner;

    public DefaultView(final Runner runner, final Stage primaryStage) {
        setRunner(runner);
        setPrimaryStage(primaryStage);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Runner getRunner() {
        return runner;
    }

    public void setRunner(Runner runner) {
        this.runner = runner;
    }

    public abstract void notifyByComponent(String action);

}
