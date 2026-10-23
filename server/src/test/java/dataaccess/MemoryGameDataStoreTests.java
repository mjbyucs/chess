package dataaccess;

import chess.ChessGame;
import dataaccess.memory.MemoryGameDataStore;
import dataaccess.memory.MemoryUserDataStore;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MemoryGameDataStoreTests {
    private static final String DEFAULT_GAME_NAME = "theGame";
    private static final String DEFAULT_USER_NAME = "theUser";
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MemoryUserDataStore userDataStore = new MemoryUserDataStore();
        userDAO = userDataStore;
        userDataStore.addUserData(new UserData(DEFAULT_USER_NAME, "", ""));
        gameDAO = new MemoryGameDataStore(userDataStore);
    }

    @Test
    void createAndRetrieve() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        Assertions.assertNotNull(gameDAO.getGameByName(DEFAULT_GAME_NAME));

        gameDAO.deleteGame(gameId);
        Assertions.assertNull(gameDAO.getGameByID(gameId));
        Assertions.assertNull(gameDAO.getGameByName(DEFAULT_GAME_NAME));
    }

    @Test
    void duplicateGameName() throws AlreadyTakenException {
        gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame())
        );
    }

    @Test
    void getAllGames() throws AlreadyTakenException {
        gameDAO.addGame(DEFAULT_GAME_NAME + "1", new ChessGame());
        gameDAO.addGame(DEFAULT_GAME_NAME + "2", new ChessGame());
        gameDAO.addGame(DEFAULT_GAME_NAME + "3", new ChessGame());
        List<GameData> allGames = gameDAO.getAllGames();
        Assertions.assertEquals(3, allGames.size());
        Assertions.assertNotEquals(allGames.get(0).gameName(), allGames.get(1).gameName());
        Assertions.assertNotEquals(allGames.get(0).gameName(), allGames.get(2).gameName());
        Assertions.assertNotEquals(allGames.get(1).gameName(), allGames.get(2).gameName());
    }

    @Test
    void updatePlayerNames() throws AlreadyTakenException {
        userDAO.addUserData(new UserData(DEFAULT_USER_NAME + "White", "", ""));
        userDAO.addUserData(new UserData(DEFAULT_USER_NAME + "Black", "", ""));
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        GameData gameData = gameDAO.getGameByID(gameId);
        Assertions.assertNull(gameData.whiteUsername());
        Assertions.assertNull(gameData.blackUsername());
        gameDAO.updatePlayerNames(gameId, DEFAULT_USER_NAME+"White", null);
        gameData = gameDAO.getGameByID(gameId);
        Assertions.assertEquals(DEFAULT_USER_NAME + "White", gameData.whiteUsername());
        Assertions.assertNull(gameData.blackUsername());
        gameDAO.updatePlayerNames(gameId, DEFAULT_USER_NAME+"White", DEFAULT_USER_NAME+"Black");
        gameData = gameDAO.getGameByID(gameId);
        Assertions.assertEquals(DEFAULT_USER_NAME + "White", gameData.whiteUsername());
        Assertions.assertEquals(DEFAULT_USER_NAME + "Black", gameData.blackUsername());
    }

    @Test
    void updatePlayerNamesWithMissingPlayer() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameDAO.updatePlayerNames(gameId, null, "No one")
        );
    }

    @Test
    void gameDeletion() throws AlreadyTakenException {
        // no game - shouldn't fail
        gameDAO.deleteGame(99);
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        gameDAO.deleteGame(gameId);
        Assertions.assertNull(gameDAO.getGameByID(gameId));
    }
}
