package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import model.AuthorizationRequest;
import model.CreateGameRequest;
import model.GameData;
import model.JoinGameRequest;
import org.jetbrains.annotations.NotNull;
import service.GameService;

import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private record GameListEntry(int gameID, String whiteUsername, String blackUsername, String gameName) {}

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCreateGame(@NotNull Context context) throws UnauthorizedException, AlreadyTakenException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        try {
            CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);
            if (createGameRequest == null) {
                throw new IllegalArgumentException("Error: bad request");
            }
            int gameID = gameService.createGame(authorizationRequest, createGameRequest.gameName());
            sendJsonResults(context, Map.of("gameID", gameID));
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Error: bad request", e);
        }
    }

    public void handleJoinGame(@NotNull Context context) throws UnauthorizedException, AlreadyTakenException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        try {
            JoinGameRequest joinGameRequest = new Gson().fromJson(context.body(), JoinGameRequest.class);
            if (joinGameRequest == null) {
                throw new IllegalArgumentException("Error: bad request");
            }
            gameService.joinGame(authorizationRequest, joinGameRequest.gameID(), joinGameRequest.playerColor());
            sendJsonResults(context, Map.of());
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Error: bad request", e);
        }
    }

    public void handleListGames(@NotNull Context context) throws UnauthorizedException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        List<GameData> gameList = gameService.listGames(authorizationRequest);
        List<GameListEntry> gameEntries = gameList.stream().map(this::translateGameData).toList();
        sendJsonResults(context, Map.of("games", gameEntries));
    }

    private GameListEntry translateGameData(GameData gameData) {
        return new GameListEntry(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
    }

    private void sendJsonResults(Context context, Object value) {
        context.contentType("application/json");
        context.result(gson.toJson(value));
    }
}
