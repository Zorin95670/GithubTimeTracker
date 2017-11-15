package com.gtt.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Service to provides access to Github api.
 *
 * @author moitt
 *
 */
public class GithubService {

    public static final String GITHUB_URL = "https://api.github.com/";

    private HttpService httpService;
    private String login;
    private String password;

    public GithubService(final String login, final String password) {
        this.login = login;
        this.password = password;

        httpService = new HttpService();
    }

    public JsonNode getAllRepositoryByOwner() {
        JsonNode response = convertStringToJson(
                httpService.get(GITHUB_URL + "user/repos", this.login, this.password, "{}"));

        ObjectNode data = JsonNodeFactory.instance.objectNode();

        if (response == null) {
            return data;
        }

        for (int i = 0; i < response.size(); i++) {
            String owner = response.get(i).get("owner").get("login").asText();
            if (!data.hasNonNull(owner)) {
                data.set(owner, JsonNodeFactory.instance.arrayNode());
            }
            ArrayNode repositories = (ArrayNode) data.get(owner);
            ObjectNode repository = JsonNodeFactory.instance.objectNode();

            repository.put("id", response.get(i).get("id").asText());
            repository.put("name", response.get(i).get("name").asText());
            repository.put("url", response.get(i).get("url").asText());
            repository.put("owner", owner);

            repositories.add(repository);
        }

        return data;
    }

    public ArrayNode getAllRepository() {
        JsonNode response = convertStringToJson(
                httpService.get(GITHUB_URL + "user/repos", this.login, this.password, "{}"));

        ArrayNode repositories = JsonNodeFactory.instance.arrayNode();

        if (response == null) {
            return repositories;
        }

        for (int i = 0; i < response.size(); i++) {
            ObjectNode repository = JsonNodeFactory.instance.objectNode();

            repository.put("id", response.get(i).get("id").asText());
            repository.put("name", response.get(i).get("name").asText());
            repository.put("url", response.get(i).get("url").asText());
            repository.put("owner", response.get(i).get("owner").get("login").asText());

            repositories.add(repository);
        }

        return repositories;
    }

    private JsonNode convertStringToJson(final String response) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readTree(response);
        } catch (IOException e) {
            return null;
        }
    }

    public ObjectNode loadIssue(String user, String repository, String issueNumber, String time) {
        String url = GITHUB_URL + "repos/" + user + "/" + repository + "/issues/" + issueNumber;
        JsonNode response = convertStringToJson(httpService.get(url, this.login, this.password, "{}"));

        if (!response.hasNonNull("id")) {
            // TODO error gestion
            return (ObjectNode) response;
        }

        ObjectNode issue = JsonNodeFactory.instance.objectNode();

        issue.put("issue", response.get("number").asText());
        issue.put("title", response.get("title").asText());
        issue.put("user", user);
        issue.put("repository", repository);
        issue.put("start", time);

        return issue;
    }

    public boolean valideUser(ObjectNode response) {
        String url = GITHUB_URL;

        JsonNode httpResponse = convertStringToJson(httpService.get(url, this.login, this.password, "{}"));

        if (httpResponse.hasNonNull("message")) {
            response.put("message", httpResponse.get("message").asText());
            return false;
        }

        return true;
    }

    public void updateActivity(final JsonNode activity) {
        String url = GITHUB_URL + "repos/" + activity.get("user").asText() + "/" + activity.get("repository").asText()
                + "/issues/" + activity.get("issue").asText();
        JsonNode response = convertStringToJson(httpService.get(url, this.login, this.password, "{}"));

        ((ObjectNode) activity).put("title", response.get("title").asText(""));

    }
}
