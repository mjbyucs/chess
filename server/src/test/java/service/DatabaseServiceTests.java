package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memory.MemoryAuthDataStore;
import dataaccess.memory.MemoryGameDataStore;
import dataaccess.memory.MemoryUserDataStore;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseServiceTests {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() {
        MemoryUserDataStore userDataStore = new MemoryUserDataStore();
        authDAO = new MemoryAuthDataStore();
        gameDAO = new MemoryGameDataStore(userDataStore);
        userDAO = userDataStore;
        databaseService = new DatabaseService(authDAO, gameDAO, userDAO);
    }

    @Test
    void deleteDatabase() throws AlreadyTakenException {
        String username = "name";
        authDAO.addAuthData(new AuthData("token", username));
        int gameId = gameDAO.addGame( "mygame", new ChessGame());
        userDAO.addUserData(new UserData(username, "password", "email"));

        Assertions.assertNotNull(authDAO.getAuthData("token"));
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        Assertions.assertNotNull(userDAO.getUserData(username));

        databaseService.deleteDatabase();

        Assertions.assertNull(authDAO.getAuthData("token"));
        Assertions.assertNull(gameDAO.getGameByID(gameId));
        Assertions.assertNull(userDAO.getUserData(username));
    }
}
