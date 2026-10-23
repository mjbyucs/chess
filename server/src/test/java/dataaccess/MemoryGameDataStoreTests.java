package dataaccess;

import chess.ChessGame;
import dataaccess.memory.MemoryGameDataStore;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MemoryGameDataStoreTests {
    private static final int DEFAULT_GAME_ID = 99;
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() {
        gameDAO = new MemoryGameDataStore();
    }

    @Test
    void testCreateAndRetrieve() throws AlreadyTakenException {
        String gameName = "theGame";
        GameData gameData = new GameData(DEFAULT_GAME_ID, "white", "black",
                                gameName, new ChessGame());
        gameDAO.createGame(gameData);
        Assertions.assertEquals(gameData, gameDAO.getGameByID(DEFAULT_GAME_ID));
        Assertions.assertEquals(gameData, gameDAO.getGameByName(gameName));

        gameDAO.deleteDatabase();
        Assertions.assertNull(gameDAO.getGameByID(DEFAULT_GAME_ID));
        Assertions.assertNull(gameDAO.getGameByName(gameName));
    }

    @Test
    void testDuplicateGameID() throws AlreadyTakenException {
        GameData gameData = new GameData(DEFAULT_GAME_ID, "", "", "", null);
        GameData otherGameData = new GameData(DEFAULT_GAME_ID, "", "", "other", null);
        gameDAO.createGame(gameData);
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> gameDAO.createGame(otherGameData)
        );
    }

    @Test
    void testDuplicateGameName() throws AlreadyTakenException {
        GameData gameData = new GameData(DEFAULT_GAME_ID, "", "", "game", null);
        GameData otherGameData = new GameData(DEFAULT_GAME_ID + 1, "", "", "game", null);
        gameDAO.createGame(gameData);
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> gameDAO.createGame(otherGameData)
        );
    }
}
