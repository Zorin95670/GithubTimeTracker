package com.gtt.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

/**
 * Service to provides http request.
 *
 * @author moitt
 *
 */
public class HttpService {

    public static final String HTTP_CONTENT_TYPE_JSON = "application/json";

    /**
     * a.
     */
    public String get(final String targetUrl, final String user, final String password, final String data) {
        HttpsURLConnection connection = null;
        String response = null;
        try {
            URL url = new URL(targetUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            setAuthentification(connection, user, password);

            response = getResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public void sendRequest(final HttpURLConnection connection, final String data) throws IOException {
        if (connection == null) {
            return;
        }

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(data);
        wr.close();
    }

    public String getResponse(final HttpURLConnection connection) throws IOException {
        if (connection == null) {
            return null;
        }

        InputStream is = null;

        if (connection.getResponseCode() != 200) {
            is = connection.getErrorStream();
        } else {
            is = connection.getInputStream();
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();

        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        return response.toString();
    }

    public void setAuthentification(final HttpURLConnection connection, final String user, final String password) {
        if (connection == null) {
            return;
        }

        String userpassword = user + ":" + password;
        connection.setRequestProperty("Authorization",
                "Basic " + new String(Base64.getEncoder().encode(userpassword.getBytes())));
    }
}
