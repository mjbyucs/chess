package server;

import handler.GameHandler;
import handler.UserHandler;
import handler.DataStoreHandler;
import io.javalin.*;

public class Server {

    private final Javalin javalin;

    public Server(UserHandler userHandler, GameHandler gameHandler, DataStoreHandler dataStoreHandler) {

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config ->
                        config.staticFiles.add("web"))
                .post("/register", userHandler::handleRegistration)
                .post("/session", userHandler::handleLogin)
                .delete("/session", userHandler::handleLogout)
                .get("/game", gameHandler::handleListGames)
                .post("/game", gameHandler::handleCreateGame)
                .put("/game", gameHandler::handleJoinGame)
                .delete("/db", dataStoreHandler::handleDeleteDatabase);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
