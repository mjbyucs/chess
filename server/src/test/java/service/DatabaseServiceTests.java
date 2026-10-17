package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
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
        authDAO = new MemoryAuthDataStore();
        gameDAO = new MemoryGameDataStore();
        userDAO = new MemoryUserDataStore();
        databaseService = new DatabaseService(authDAO, gameDAO, userDAO);
    }

    @Test
    void deleteDatabase() throws AlreadyTakenException {
        String username = "name";
        authDAO.addAuthData(new AuthData("token", username));
        gameDAO.createGame(new GameData(1, null, null, "mygame", null));
        userDAO.addUserData(new UserData(username, "password", "email"));

        Assertions.assertNotNull(authDAO.getAuthData("token"));
        Assertions.assertNotNull(gameDAO.getGameByID(1));
        Assertions.assertNotNull(userDAO.getUserData(username));

        databaseService.deleteDatabase();

        Assertions.assertNull(authDAO.getAuthData("token"));
        Assertions.assertNull(gameDAO.getGameByID(1));
        Assertions.assertNull(userDAO.getUserData(username));
    }
}
