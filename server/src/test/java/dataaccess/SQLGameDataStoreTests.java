package dataaccess;

import chess.ChessGame;
import dataaccess.sql.SQLGameDataStore;
import dataaccess.sql.SQLUserDataStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SQLGameDataStoreTests extends AbstractGameDataStoreTests {
    private SQLTestHelper sqlTestHelper;
    private SQLUserDataStore userDataStore;

    @Override
    protected UserDAO createUserDAO() {
        sqlTestHelper = new SQLTestHelper();
        DatabaseManager.createDatabase();
        userDataStore = new SQLUserDataStore(DatabaseManager::getConnection);
        return userDataStore;
    }

    @Override
    protected GameDAO createGameDAO() {
        return new SQLGameDataStore(sqlTestHelper::getConnection, userDataStore);
    }

    @Override
    protected void tearDownDAOs() {
        sqlTestHelper.disableConnectionFailures();
        gameDAO.clearDatabase();
        userDAO.clearDatabase();
    }

    @Test
    void sqlExceptionOnAddGame() {
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame())
        );
    }

    @Test
    void sqlExceptionOnGetGameByID() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.getGameByID(gameId)
        );
    }

    @Test
    void sqlExceptionOnGetGameByName() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.getGameByName(DEFAULT_GAME_NAME)
        );
    }

    @Test
    void sqlExceptionOnUpdatePlayerID() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.updatePlayerNames(gameId, DEFAULT_USER_NAME, null)
        );
    }

    @Test
    void sqlExceptionOnGetAllGames() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.getAllGames()
        );
    }

    @Test
    void sqlExceptionOnDeleteGame() throws AlreadyTakenException {
        int gameId = gameDAO.addGame(DEFAULT_GAME_NAME, new ChessGame());
        Assertions.assertNotNull(gameDAO.getGameByID(gameId));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> gameDAO.deleteGame(gameId)
        );
    }
}
