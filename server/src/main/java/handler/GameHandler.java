package handler;

import com.google.gson.Gson;
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

    private record GameListEntry(int gameID, String whiteUsername, String blackUsername, String gameName) {}

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCreateGame(@NotNull Context context) throws UnauthorizedException, AlreadyTakenException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);
        int gameID = gameService.createGame(authorizationRequest, createGameRequest.gameName());
        context.result(new Gson().toJson(Map.of("gameID", gameID)));
    }

    public void handleJoinGame(@NotNull Context context) throws UnauthorizedException, AlreadyTakenException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        JoinGameRequest joinGameRequest = new Gson().fromJson(context.body(), JoinGameRequest.class);
        gameService.joinGame(authorizationRequest, joinGameRequest.gameID(), joinGameRequest.playerColor());
    }

    public void handleListGames(@NotNull Context context) throws UnauthorizedException {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(context.header("Authorization"));
        List<GameData> gameList = gameService.listGames(authorizationRequest);
        List<GameListEntry> gameEntries = gameList.stream().map(this::translateGameData).toList();
        context.result(new Gson().toJson(Map.of("games", gameEntries)));
    }

    private GameListEntry translateGameData(GameData gameData) {
        return new GameListEntry(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
    }
}
