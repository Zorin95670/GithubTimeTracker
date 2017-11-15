package com.gtt.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gtt.services.GithubService;
import com.gtt.services.ORMService;

public class Apps {
    private static Apps instance = new Apps();
    private GithubService github;
    private ORMService orm;
    private Properties config;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    public SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

    private Apps() {
        initConfig();
    }

    public static final String USER_LOGIN_PREFERENCE = "preference.user.login";

    public void initConfig() {
        config = new Properties();

        try {
            config.load(new FileInputStream("config.ini"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean init(final ObjectNode response, final String login, final String password) {
        if (!initGithub(response, login, password)) {
            return false;
        }

        if (!initORM(response)) {
            return false;
        }

        return true;
    }

    public static boolean initGithub(final ObjectNode response, final String login, final String password) {
        instance.github = new GithubService(login, password);

        return instance.github.valideUser(response);
    }

    public static boolean initORM(final ObjectNode response) {
        instance.orm = new ORMService(getProperty("data.path"));
        setProperty("data.path", instance.orm.getDefaultPath());

        return instance.orm.initTable(response);
    }

    public static String getProperty(String key) {
        String value = instance.config.getProperty(key);

        if (value == null) {
            return "";
        }
        return value;
    }

    public static void setProperty(String key, String value) {
        instance.config.setProperty(key, value);
        instance.saveProperties();
    }

    public static void removeProperty(String key) {
        instance.config.remove(key);
        instance.saveProperties();
    }

    private void saveProperties() {
        try {
            instance.config.store(new FileOutputStream("config.ini"), "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static GithubService github() {
        return instance.github;
    }

    public static ORMService orm() {
        return instance.orm;
    }

    public static String getCurrentTime() {
        return instance.timeFormat.format(new Date());
    }

    public static String getCurrentDate() {
        return instance.dateFormat.format(new Date());
    }

    public static Date parseDate(final String date) {
        try {
            return instance.dateFormat.parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Date();
    }

    public static Date parseTime(final String time) {
        try {
            return instance.timeFormat.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Date();
    }

    public static String formatDate(final Date date) {
        return instance.dateFormat.format(date);
    }

    public static String formatTime(final Date time) {
        return instance.timeFormat.format(time);
    }

    public static String getTime(String start, String end) {

        long second = (parseTime(end).getTime() - parseTime(start).getTime()) / 1000;
        long minute = second / 60;
        long hour = minute / 60;

        minute = minute - hour * 60;

        String h, m;

        if (hour < 10) {
            h = "0";
        } else {
            h = "";
        }

        if (minute < 10) {
            m = "0";
        } else {
            m = "";
        }

        return h + hour + ":" + m + minute;
    }

    public static boolean isSameActivity(JsonNode a, JsonNode b) {
        if (!a.get("user").asText().equals(b.get("user").asText())) {
            return false;
        }

        if (!a.get("repository").asText().equals(b.get("repository").asText())) {
            return false;
        }

        return a.get("issue").asText().equals(b.get("issue").asText());
    }

	public static ScheduledExecutorService getScheduled() {
		return instance.scheduled;
	}
}
