package com.gtt.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gtt.core.Apps;
import com.gtt.core.GTTTable;

public class ORMService {

    private String defaultPath;
    private String dbName = "GithubTimeTracker.db";
    private Connection connection;
    private SimpleDateFormat ormFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat ormDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat ormTimeFormat = new SimpleDateFormat(" HH:mm:ss.SSS");

    public ORMService(final String defaultPath) {
        setDefaultPath(defaultPath);
    }

    public boolean initConnection(final ObjectNode response) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDefaultPath() + File.separator + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", e.getMessage());
            return false;
        }

        return true;
    }

    public Connection getConnection(final ObjectNode response) throws SQLException {
        if (connection == null || connection.isClosed()) {
            initConnection(response);
        }

        return connection;
    }

    public boolean initTable(final ObjectNode response) {
        if (!createTable(response, GTTTable.ACTIVITIES)) {
            return false;
        }

        return createTable(response, GTTTable.TRASH);
    }

    public boolean createTable(final ObjectNode response, final GTTTable table) {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS " + table.getName() + "(");
        query.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        query.append("date TEXT, ");
        query.append("start TEXT, ");
        query.append("end TEXT, ");
        query.append("user TEXT, ");
        query.append("repository TEXT, ");
        query.append("issue INTEGER ");
        query.append("); ");

        try {
            Connection connection = getConnection(response);

            Statement statement = connection.createStatement();

            statement.execute(query.toString());
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean update(final ObjectNode response, final JsonNode activity, final GTTTable table) {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE " + table.getName() + " SET ");
        query.append("date = ?, ");
        query.append("start = ?, ");
        query.append("end = ?, ");
        query.append("user = ?, ");
        query.append("repository = ?, ");
        query.append("issue = ? ");
        query.append("WHERE id = ?");

        try {
            Connection connection = getConnection(response);

            PreparedStatement statement = connection.prepareStatement(query.toString());

            statement.setString(1, getformatedDate(activity.get("date").asText()));
            statement.setString(2, getformatedTime(activity.get("date").asText(), activity.get("start").asText()));

            if (activity.hasNonNull("end")) {
                statement.setString(3, getformatedTime(activity.get("date").asText(), activity.get("end").asText()));
            } else {
                statement.setString(3, null);
            }

            statement.setString(4, activity.get("user").asText());
            statement.setString(5, activity.get("repository").asText());
            statement.setString(6, activity.get("issue").asText());
            statement.setString(7, activity.get("id").asText());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean insert(final ObjectNode response, final JsonNode activity, final GTTTable table) {
        String query = "INSERT INTO " + table.getName()
                + " (date, start, end, user, repository, issue) VALUES(?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection(response);

            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, getformatedDate(activity.get("date").asText()));
            statement.setString(2, getformatedTime(activity.get("date").asText(), activity.get("start").asText()));

            if (activity.hasNonNull("end")) {
                statement.setString(3, getformatedTime(activity.get("date").asText(), activity.get("end").asText()));
            } else {
                statement.setString(3, null);
            }

            statement.setString(4, activity.get("user").asText());
            statement.setString(5, activity.get("repository").asText());
            statement.setString(6, activity.get("issue").asText());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ((ObjectNode) activity).put("id", generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", e.getMessage());
            return false;
        }

        return true;
    }

    public ArrayNode loadCurrent(final ObjectNode response, final ArrayNode activities, final GTTTable table) {
        String query = "SELECT id, date, start, end, user, repository, issue FROM " + table.getName()
                + " WHERE date = ?";

        try {
            Connection connection = getConnection(response);

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, getformatedDate(Apps.getCurrentDate()));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ObjectNode activity = JsonNodeFactory.instance.objectNode();

                activity.put("id", resultSet.getString("id"));
                activity.put("date", Apps.formatDate(ormFormat.parse(resultSet.getString("date"))));
                activity.put("start", Apps.formatTime(ormFormat.parse(resultSet.getString("start"))));

                String end = resultSet.getString("end");

                if (end != null) {
                    activity.put("end", Apps.formatTime(ormFormat.parse(end)));
                    activity.put("time", Apps.getTime(activity.get("start").asText(), activity.get("end").asText()));
                }

                activity.put("user", resultSet.getString("user"));
                activity.put("repository", resultSet.getString("repository"));
                activity.put("issue", resultSet.getString("issue"));

                activities.add(activity);
            }
        } catch (Exception e) {
            response.put("message", e.getMessage());
        }

        return activities;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        if (defaultPath == null) {
            this.defaultPath = new File(System.getProperty("user.home")).getAbsolutePath();
            return;
        }

        File path = new File(defaultPath);

        if (!path.exists() || !path.isDirectory()) {
            setDefaultPath(null);
            return;
        }

        this.defaultPath = path.getAbsolutePath();
    }

    public boolean delete(ObjectNode response, JsonNode activity, final GTTTable table) {
        String query = "DELETE FROM " + table.getName() + " WHERE id = ?";

        try {
            Connection connection = getConnection(response);

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, activity.get("id").asText());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", e.getMessage());
            return false;
        }

        return insert(response, activity, GTTTable.TRASH);
    }

    public String getformatedDate(final String date) {
        return ormFormat.format(Apps.parseDate(date));
    }

    public String getformatedTime(final String date, final String time) {
        StringBuilder result = new StringBuilder();

        result.append(ormDateFormat.format(Apps.parseDate(date)));
        result.append(ormTimeFormat.format(Apps.parseTime(time)));

        return result.toString();
    }
}
