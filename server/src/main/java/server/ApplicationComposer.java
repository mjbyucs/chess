package server;

import dataaccess.*;
import handler.DataStoreHandler;
import handler.GameHandler;
import handler.UserHandler;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.DatabaseService;
import service.GameService;
import service.UserService;

public class ApplicationComposer {

    public static Server createMemoryServer() {
        AuthDAO authDAO = new MemoryAuthDataStore();
        GameDAO gameDAO = new MemoryGameDataStore();
        UserDAO userDAO = new MemoryUserDataStore();

        return createServer(authDAO, gameDAO, userDAO);
    }

    public static Server createDatabaseServer() {
        throw new UnsupportedOperationException("No database support (yet).");
    }


    @NotNull
    private static Server createServer(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        AuthService authService = new AuthService(authDAO);
        GameService gameService = new GameService(gameDAO, authService);
        UserService userService = new UserService(userDAO, authService);
        DatabaseService databaseService = new DatabaseService(authDAO, gameDAO, userDAO);

        GameHandler gameHandler = new GameHandler(gameService);
        UserHandler userHandler = new UserHandler(userService);
        DataStoreHandler dataStoreHandler = new DataStoreHandler(databaseService);

        return new Server(userHandler, gameHandler, dataStoreHandler);
    }
}
