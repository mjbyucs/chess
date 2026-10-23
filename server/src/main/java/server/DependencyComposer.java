package server;

import dataaccess.*;
import dataaccess.memory.MemoryAuthDataStore;
import dataaccess.memory.MemoryGameDataStore;
import dataaccess.memory.MemoryUserDataStore;
import dataaccess.sql.SQLAuthDataStore;
import dataaccess.sql.SQLGameDataStore;
import dataaccess.sql.SQLUserDataStore;
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
        this(StorageType.DATABASE, null);
    }
    public DependencyComposer(StorageType storageType, String databasePropertiesFilename) {
        AuthDAO authDAO;
        UserDAO userDAO;
        GameDAO gameDAO;
        if (storageType == StorageType.MEMORY) {
            MemoryUserDataStore userDataStore = new MemoryUserDataStore();
            authDAO = new MemoryAuthDataStore();
            gameDAO = new MemoryGameDataStore(userDataStore);
            userDAO = userDataStore;
        }
        else {
            DatabaseManager.createDatabaseWithResources(databasePropertiesFilename);
            SQLUserDataStore userDataStore = new SQLUserDataStore(DatabaseManager::getConnection);
            authDAO = new SQLAuthDataStore(DatabaseManager::getConnection, userDataStore);
            gameDAO = new SQLGameDataStore(DatabaseManager::getConnection, userDataStore);
            userDAO = userDataStore;
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
