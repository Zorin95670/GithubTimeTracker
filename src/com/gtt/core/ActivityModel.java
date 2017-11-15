package com.gtt.core;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.beans.property.SimpleStringProperty;

public class ActivityModel {
    private SimpleStringProperty start;
    private SimpleStringProperty end;
    private SimpleStringProperty project;
    private SimpleStringProperty issue;
    private SimpleStringProperty description;
    private SimpleStringProperty time;
    private SimpleStringProperty id;

    public ActivityModel(final JsonNode activity) {
        setActivity(activity);
    }

    public void setActivity(final JsonNode activity) {
        if (activity.hasNonNull("start")) {
            start = new SimpleStringProperty(activity.get("start").asText());
        } else {
            start = new SimpleStringProperty("");
        }

        if (activity.hasNonNull("end")) {
            end = new SimpleStringProperty(activity.get("end").asText());
        } else {
            end = new SimpleStringProperty("");
        }

        project = new SimpleStringProperty(activity.get("repository").asText());

        issue = new SimpleStringProperty(activity.get("issue").asText());

        description = new SimpleStringProperty(activity.get("title").asText());

        if (activity.hasNonNull("time")) {
            time = new SimpleStringProperty(activity.get("time").asText());
        } else {
            time = new SimpleStringProperty("");
        }

        if (activity.hasNonNull("id")) {
            id = new SimpleStringProperty(activity.get("id").asText());
        } else {
            id = new SimpleStringProperty("");
        }
    }

    public String getStart() {
        return start.get();
    }

    public void setStart(String start) {
        this.start.set(start);
    }

    public String getEnd() {
        return end.get();
    }

    public void setEnd(String end) {
        this.end.set(end);
    }

    public String getProject() {
        return project.get();
    }

    public void setProject(String project) {
        this.project.set(project);
    }

    public String getIssue() {
        return issue.get();
    }

    public void setIssue(String issue) {
        this.issue.set(issue);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getID() {
        return id.get();
    }

    public void setID(String id) {
        this.time.set(id);
    }
}
