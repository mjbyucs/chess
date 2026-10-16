package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import handler.GameHandler;
import handler.UserHandler;
import handler.DataStoreHandler;
import io.javalin.*;
import io.javalin.http.HttpStatus;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        this(new DependencyComposer());
    }
    public Server(DependencyComposer dependencyComposer) {
        UserHandler userHandler = dependencyComposer.getUserHandler();
        GameHandler gameHandler = dependencyComposer.getGameHandler();
        DataStoreHandler dataStoreHandler = dependencyComposer.getDataStoreHandler();

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config ->
                        config.staticFiles.add("web"))
                .post("/register", userHandler::handleRegistration)
                .post("/session", userHandler::handleLogin)
                .delete("/session", userHandler::handleLogout)
                .get("/game", gameHandler::handleListGames)
                .post("/game", gameHandler::handleCreateGame)
                .put("/game", gameHandler::handleJoinGame)
                .delete("/db", dataStoreHandler::handleDeleteDatabase)
                .exception(IllegalArgumentException.class,
                        (ex, ctx) -> {ctx.status(HttpStatus.BAD_REQUEST.getCode());
                                                                    ctx.result(formatError("Error: bad request"));})
                .exception(UnauthorizedException.class,
                        (ex, ctx) -> {ctx.status(HttpStatus.UNAUTHORIZED.getCode());
                                                                    ctx.result(formatError("Error: unauthorized"));})
                .exception(AlreadyTakenException.class,
                        (ex, ctx) -> {ctx.status(HttpStatus.FORBIDDEN.getCode());
                                                                    ctx.result(formatError("Error: already taken"));})
                .exception(DataAccessException.class,
                        (ex, ctx) -> {ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                                                                ctx.result(formatError("Error: database failure"));})
                .exception(Exception.class,
                        (ex, ctx) -> {ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
                                                        ctx.result(formatError(ex.getMessage()));});

    }

    String formatError(String message) {
        return new Gson().toJson(Map.of("message", message));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
