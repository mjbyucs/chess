package server;

import dataaccess.*;
import handler.DataStoreHandler;
import handler.GameHandler;
import handler.UserHandler;
import service.AuthService;
import service.DatabaseService;
import service.GameService;
import service.UserService;

public class DependencyComposer {
    private final GameHandler gameHandler;
    private final UserHandler userHandler;
    private final DataStoreHandler dataStoreHandler;

    public enum StorageType {
        MEMORY,
        DATABASE
    }

    /**
     * Default constructor creates a memory based storage system
     */
    public DependencyComposer() {
        this(StorageType.MEMORY);
    }
    public DependencyComposer(StorageType storageType) {
        AuthDAO authDAO;
        UserDAO userDAO;
        GameDAO gameDAO;
        if (storageType == StorageType.MEMORY) {
            authDAO = new MemoryAuthDataStore();
            gameDAO = new MemoryGameDataStore();
            userDAO = new MemoryUserDataStore();
        }
        else {
            throw new UnsupportedOperationException("No database support (yet).");
        }
        AuthService authService = new AuthService(authDAO);
        this.gameHandler = createGameHandler(gameDAO, authService);
        this.userHandler = createUserHandler(userDAO, authService);
        this.dataStoreHandler = createDataStoreHandler(authDAO, gameDAO, userDAO);
    }

    private GameHandler createGameHandler(GameDAO gameDAO, AuthService authService) {
        return new GameHandler(new GameService(gameDAO, authService));
    }

    private UserHandler createUserHandler(UserDAO userDAO, AuthService authService) {
        return new UserHandler(new UserService(userDAO, authService));
    }

    private DataStoreHandler createDataStoreHandler(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        return new DataStoreHandler(new DatabaseService(authDAO, gameDAO, userDAO));
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public DataStoreHandler getDataStoreHandler() {
        return dataStoreHandler;
    }
}
