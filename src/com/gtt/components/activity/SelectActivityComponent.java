package com.gtt.components.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gtt.components.DefaultComponent;
import com.gtt.core.Apps;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SelectActivityComponent extends DefaultComponent {

    private ChoiceBox<String> users;
    private ChoiceBox<String> repositories;
    private TextField issue;
    private Button submit;
    private HashMap<String, List<String>> map = new HashMap<>();
    private ObjectNode lastIssue;
    private JsonNode repositoriesData;

    public SelectActivityComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SelectActivityComponent.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();

            users = (ChoiceBox<String>) this.lookup("#SelectActivityComponentUsers");
            repositories = (ChoiceBox<String>) this.lookup("#SelectActivityComponentRepositories");
            issue = (TextField) this.lookup("#SelectActivityComponentIssue");
            submit = (Button) this.lookup("#SelectActivityComponentSubmit");

            initEvents();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void initEvents() {
        users.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                ObservableList<String> items = FXCollections.observableArrayList();
                String user = users.getItems().get(newValue.intValue());

                for (int i = 0; i < repositoriesData.get(user).size(); i++) {
                    items.add(repositoriesData.get(user).get(i).get("name").asText());
                }

                repositories.setItems(items);
            }
        });
        submit.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setLastIssue(null);
                String selectedUser = users.getValue();
                String selectedRepository = repositories.getValue();
                String selectedIssue = issue.getText();

                setLastIssue(Apps.github().loadIssue(selectedUser, selectedRepository, selectedIssue,
                        Apps.getCurrentTime()));

                getRoot().notifyByComponent("start activity");
            }
        });
    }

    public ObjectNode getLastIssue() {
        return lastIssue;
    }

    public void setLastIssue(ObjectNode lastIssue) {
        this.lastIssue = lastIssue;
    }

    public void clearLastIsssue() {
        this.lastIssue = null;
    }

    public void setRepositories(JsonNode repositories) {
        this.repositoriesData = repositories;

        ObservableList<String> items = FXCollections.observableArrayList();

        Iterator<String> owner = repositories.fieldNames();
        while (owner.hasNext()) {
            items.add(owner.next());
        }

        users.setItems(items);
    }
}
