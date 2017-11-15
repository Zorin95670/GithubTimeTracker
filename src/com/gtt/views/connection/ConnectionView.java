package com.gtt.views.connection;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gtt.core.Apps;
import com.gtt.views.DefaultView;
import com.gtt.views.Runner;
import com.gtt.views.timetracker.TimeTrackerView;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ConnectionView extends DefaultView {

    public ConnectionView(Runner runner, Stage primaryStage) {
        super(runner, primaryStage);

        initRootLayout();
        initEvents();
    }

    private BorderPane rootLayout;

    private TextField userLogin;
    private PasswordField userPwd;
    private Button submit;
    private CheckBox prefLogin;
    private Label error;
    private ProgressBar loadingBar;

    private String defaultLogin = Apps.getProperty(Apps.USER_LOGIN_PREFERENCE);

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ConnectionView.fxml"));
            rootLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLayout);

            getPrimaryStage().setScene(scene);
            getPrimaryStage().show();

            userLogin = (TextField) scene.lookup("#userLogin");
            userPwd = (PasswordField) scene.lookup("#userPwd");
            submit = (Button) scene.lookup("#submit");
            prefLogin = (CheckBox) scene.lookup("#prefLogin");
            error = (Label) scene.lookup("#error");
            loadingBar = (ProgressBar) scene.lookup("#loadingBar");

            if (defaultLogin != null && defaultLogin.length() > 0) {
                userLogin.setText(defaultLogin);
                prefLogin.setSelected(true);
                userPwd.requestFocus();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void initEvents() {
        submit.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                submit();
            }
        });

        prefLogin.setOnAction((event) -> {
            if (prefLogin.isSelected() && !defaultLogin.equals(userLogin.getText())) {
                Apps.setProperty(Apps.USER_LOGIN_PREFERENCE, userLogin.getText());
                defaultLogin = userLogin.getText();
            } else if (prefLogin.isSelected()) {
                Apps.removeProperty(Apps.USER_LOGIN_PREFERENCE);
                defaultLogin = "";
            }
        });

        userLogin.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!defaultLogin.equals(userLogin.getText())) {
                    prefLogin.setSelected(false);
                }
            }
        });

        userPwd.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (KeyCode.ENTER.getName().equals(event.getCode().getName())) {
                    submit();
                    event.consume();
                }
            }
        });
    }

    public void submit() {
        error.setVisible(false);
        loadingBar.setVisible(true);

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        CompletableFuture.supplyAsync(() -> {
            return Apps.init(response, userLogin.getText(), userPwd.getText());
        }).thenAccept((result) -> {
            Platform.runLater(() -> {
                if (result.booleanValue()) {
                    getRunner().changeView(new TimeTrackerView(getRunner(), getPrimaryStage()));
                } else {
                    error.setText("Error: " + response.get("message").asText());
                    error.setVisible(true);
                }
                loadingBar.setVisible(false);
            });
        });
    }

    @Override
    public void notifyByComponent(String action) {
    }
}
