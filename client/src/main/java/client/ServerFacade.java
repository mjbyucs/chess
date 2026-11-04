package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class ServerFacade {
    public static final String DEFAULT_SERVER_URL = "http://localhost";
    private static final String USER_ENDPOINT = "/user";
    private static final String GAME_ENDPOINT = "/game";
    private static final String SESSION_ENDPOINT = "/session";
    private static final String DATABASE_ENDPOINT = "/db";

    private final HttpClient client;
    private final String serverUrl;

    public ServerFacade(int port) {
        this(DEFAULT_SERVER_URL, port);
    }
    public ServerFacade(String url, int port) {
        this(url, port, HttpClient.newHttpClient());
    }
    public ServerFacade(String url, int port, HttpClient httpClient) {
        serverUrl = String.format("%s:%d", url, port);
        client = httpClient;
    }

    public AuthData registerUser(UserData userData) throws ResponseException {
        // .post("/user", userHandler::handleRegistration)
        var request = buildRequest("POST", USER_ENDPOINT, null, userData);
        return sendRequest(request, AuthData.class);
    }

    public AuthData loginUser(UserData userData) throws ResponseException {
        // .post("/session", userHandler::handleLogin)
        var request = buildRequest("POST", SESSION_ENDPOINT, null, userData);
        return sendRequest(request, AuthData.class);
    }

    public void logoutUser(AuthData authData) throws ResponseException {
        // .delete("/session", userHandler::handleLogout)
        var request = buildRequest("DELETE", SESSION_ENDPOINT, authData, null);
        sendRequest(request, AuthData.class);
    }

    public void listGames(AuthData authData) {
        // .get("/game", gameHandler::handleListGames)
    }

    public void createGame(AuthData authData, String gameName) {
        // .post("/game", gameHandler::handleCreateGame)
    }

    public void joinGame(AuthData authData, String playerColor, int gameId) {
        // .put("/game", gameHandler::handleJoinGame)
    }

    public void deleteDatabase() throws ResponseException {
        // .delete("/db", dataStoreHandler::handleDeleteDatabase)
        var request = buildRequest("DELETE", DATABASE_ENDPOINT, null, null);
        sendRequest(request, AuthData.class);
    }

    private HttpRequest buildRequest(String method, String path, AuthData authData, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (authData != null) {
            request.setHeader("Authorization", authData.authToken());
        }
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseClass) throws ResponseException {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, getErrorMessage(response.body(), status));
        }

        return responseClass == null ? null : new Gson().fromJson(response.body(), responseClass);
    }

    private String getErrorMessage(String json, int status) {
        if (json == null || json.isBlank()) {
            return "other failure: " + status;
        }
        else {
            var map = new Gson().fromJson(json, HashMap.class);
            return map.get("message").toString();
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
