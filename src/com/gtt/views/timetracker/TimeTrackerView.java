package com.gtt.views.timetracker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gtt.components.activity.SelectActivityComponent;
import com.gtt.core.ActivityModel;
import com.gtt.core.Apps;
import com.gtt.core.GTTTable;
import com.gtt.views.DefaultView;
import com.gtt.views.Runner;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TimeTrackerView extends DefaultView {

	private AnchorPane rootLayout;
	private SelectActivityComponent activityComponent;
	private TableView<ActivityModel> table;

	private ArrayNode activities;
	private String lastTimeSet;

	public TimeTrackerView(Runner runner, Stage primaryStage) {
		super(runner, primaryStage);

		activities = JsonNodeFactory.instance.arrayNode();

		initRootLayout();
		initEvents();
	}

	public void initRootLayout() {
		try {
			getPrimaryStage().setHeight(500);
			getPrimaryStage().setMinWidth(650);
			getPrimaryStage().setWidth(650);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("TimeTrackerView.fxml"));
			rootLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(rootLayout);
			getPrimaryStage().setScene(scene);
			getPrimaryStage().show();

			activityComponent = (SelectActivityComponent) scene.lookup("#TimeTrackerViewActivity");

			activityComponent.setRoot(this);

			table = (TableView<ActivityModel>) scene.lookup("#TimeTrackerViewTable");
			table.setEditable(true);

			TableColumn<ActivityModel, String> startCol = new TableColumn<ActivityModel, String>("Start");
			startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
			//
			TableColumn<ActivityModel, String> endCol = new TableColumn<ActivityModel, String>("End");
			endCol.setCellValueFactory(new PropertyValueFactory<>("end"));

			TableColumn<ActivityModel, String> timeCol = new TableColumn<ActivityModel, String>("Time");
			timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

			TableColumn<ActivityModel, String> projectCol = new TableColumn<ActivityModel, String>("Project");
			projectCol.setCellValueFactory(new PropertyValueFactory<>("project"));

			TableColumn<ActivityModel, String> issueCol = new TableColumn<ActivityModel, String>("Issue");
			issueCol.setCellValueFactory(new PropertyValueFactory<>("issue"));

			TableColumn<ActivityModel, String> descriptionCol = new TableColumn<ActivityModel, String>("Description");
			descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

			table.getColumns().add(startCol);
			table.getColumns().add(endCol);
			table.getColumns().add(timeCol);
			table.getColumns().add(projectCol);
			table.getColumns().add(issueCol);
			table.getColumns().add(descriptionCol);

			initRepositories();

			Apps.orm().loadCurrent(JsonNodeFactory.instance.objectNode(), activities, GTTTable.ACTIVITIES);

			for (int i = 0; i < activities.size(); i++) {
				Apps.github().updateActivity(activities.get(i));
				table.getItems().add(new ActivityModel(activities.get(i)));
			}
			
			initRefresh();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initRepositories() {
		JsonNode repositories = Apps.github().getAllRepositoryByOwner();

		activityComponent.setRepositories(repositories);
	}

	public void initEvents() {
		table.setRowFactory(tv -> {
			TableRow<ActivityModel> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					startActivity(row.getItem().getID());
				}
			});
			return row;
		});

		table.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.DELETE)) {
					ActivityModel selected = table.getSelectionModel().getSelectedItem();
					if (selected != null) {
						deleteActivity(selected.getID());
						table.getItems().remove(table.getSelectionModel().getSelectedIndex());
					}
				}
			};
		});
	}

	public void startActivity(final ObjectNode activity) {
		activity.put("date", Apps.getCurrentDate());
		activities.add(activity);
		table.getItems().add(new ActivityModel(activity));
		Apps.orm().insert(JsonNodeFactory.instance.objectNode(), activity, GTTTable.ACTIVITIES);
	}

	public void startActivity(final String id) {
		String time = Apps.getCurrentTime();

		ObjectNode activity = null;
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).get("id").asText().equals(id)) {
				activity = (ObjectNode) activities.get(i).deepCopy();
				break;
			}
		}

		if (activity == null) {
			System.out.println("null: " + id);
			return;
		}
		
		activity.put("start", time);
		activity.remove("end");
		activity.remove("id");

		stopLastActivity();
		startActivity(activity);
	}

	public void updateActivity(JsonNode activity) {
		for (int i = 0; i < table.getItems().size(); i++) {
			if (table.getItems().get(i).getID().equals(activity.get("id").asText())) {
				table.getItems().get(i).setActivity(activity);
				break;
			}
		}

		Apps.orm().update(JsonNodeFactory.instance.objectNode(), activity, GTTTable.ACTIVITIES);
	}

	public void stopLastActivity() {
		if (activities.size() == 0) {
			return;
		}

		if (activities.get(activities.size() - 1).hasNonNull("end")) {
			return;
		}
		ObjectNode activity = (ObjectNode) activities.get(activities.size() - 1);
		activity.put("end", Apps.getCurrentTime());
		activity.put("time", Apps.getTime(activity.get("start").asText(), activity.get("end").asText()));

		updateActivity(activity);
	}

	public void refreshActivities() {
		table.refresh();
	}

	public void deleteActivity(String id) {
		ObjectNode activity = null;
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).get("id").asText().equals(id)) {
				activity = (ObjectNode) activities.get(i);
				activities.remove(i);
				break;
			}
		}

		Apps.orm().delete(JsonNodeFactory.instance.objectNode(), activity, GTTTable.ACTIVITIES);

	}

	public void refreshAllTime() {
		String defaultEnd = Apps.getCurrentTime();

		if (defaultEnd.equals(lastTimeSet)) {
			return;
		}
		
		lastTimeSet = defaultEnd;
		
		for (int i = 0; i < activities.size(); i++) {
			ObjectNode activity = (ObjectNode) activities.get(i);

			if (activity.hasNonNull("end")) {
				continue;
			}

			activity.put("time", Apps.getTime(activity.get("start").asText(), defaultEnd));
			
			for (int j = 0; j < table.getItems().size() ; j++) {
				if (table.getItems().get(i).getID().equals(activity.get("id").asText())) {
					table.getItems().get(j).setActivity(activity);
					table.refresh();
					break;
				}
			}
		}
	}

	@Override
	public void notifyByComponent(final String action) {
		if ("start activity".equals(action)) {
			stopLastActivity();
			startActivity(activityComponent.getLastIssue());
			refreshActivities();
		}
	}

	public void initRefresh() {
		refreshAllTime();
		
		int initialDelay = getCurrentSecond() + 1;
		
		Apps.getScheduled().scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				refreshAllTime();
			}
			
		}, initialDelay, 10, TimeUnit.SECONDS);
	}
	
	public int getCurrentSecond() {
		return 60 - Integer.parseInt(new SimpleDateFormat("s").format(new Date()));
	}

}
