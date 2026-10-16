package handler;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.GameService;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
    public void handleListGames(@NotNull Context context) {
        // TODO: handleListGames
    }

    public void handleCreateGame(@NotNull Context context) {
        // TODO: handleCreateGame
    }

    public void handleJoinGame(@NotNull Context context) {
        // TODO: handleJoinGame
    }
}
