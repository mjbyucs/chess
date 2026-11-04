package client;

import com.google.gson.Gson;
import model.*;

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

    private record GameCreatedData(int gameID) {}
    private record GameListResponse(GameList games) {}


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
        RegisterRequest registerRequest = new RegisterRequest(userData.username(), userData.password(), userData.email());
        var request = buildRequest("POST", USER_ENDPOINT, null, registerRequest);
        return sendRequest(request, AuthData.class);
    }

    public AuthData loginUser(UserData userData) throws ResponseException {
        // .post("/session", userHandler::handleLogin)
        LoginRequest loginRequest = new LoginRequest(userData.username(), userData.password());
        var request = buildRequest("POST", SESSION_ENDPOINT, null, loginRequest);
        return sendRequest(request, AuthData.class);
    }

    public void logoutUser(AuthData authData) throws ResponseException {
        // .delete("/session", userHandler::handleLogout)
        var request = buildRequest("DELETE", SESSION_ENDPOINT, authData, null);
        sendRequest(request, null);
    }

    public GameList listGames(AuthData authData) throws ResponseException {
        // .get("/game", gameHandler::handleListGames)
        var request = buildRequest("GET", GAME_ENDPOINT, authData, null);
        GameListResponse response = sendRequest(request, GameListResponse.class);
        return response != null ? response.games() : null;
    }

    public Integer createGame(AuthData authData, String gameName) throws ResponseException {
        // .post("/game", gameHandler::handleCreateGame)
        var request = buildRequest("POST", GAME_ENDPOINT, authData, new CreateGameRequest(gameName));
        GameCreatedData gameCreatedData = sendRequest(request, GameCreatedData.class);
        return gameCreatedData != null ? gameCreatedData.gameID() : null;
    }

    public void joinGame(AuthData authData, String playerColor, int gameId) throws ResponseException {
        // .put("/game", gameHandler::handleJoinGame)
        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, gameId);
        var request = buildRequest("PUT", GAME_ENDPOINT, authData, joinGameRequest);
        sendRequest(request, null);
    }

    public void deleteDatabase() throws ResponseException {
        // .delete("/db", dataStoreHandler::handleDeleteDatabase)
        var request = buildRequest("DELETE", DATABASE_ENDPOINT, null, null);
        sendRequest(request, null);
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
