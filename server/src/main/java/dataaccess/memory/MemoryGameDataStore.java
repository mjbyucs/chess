package dataaccess.memory;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDataStore implements GameDAO {
    private int nextGameID = 1000;
    private final Map<Integer, GameData> gameDataStore = new HashMap<>();
    private final Map<String, Integer> nameToIDMap = new HashMap<>();

    private final UserDAO userDataStore;

    public MemoryGameDataStore(UserDAO userDAO) {
        this.userDataStore = userDAO;
        clearDatabase();
    }

    @Override
    public synchronized int addGame(@NotNull String gameName, @NotNull ChessGame chessGame) throws AlreadyTakenException {
        if (nameToIDMap.containsKey(gameName)) {
            throw new AlreadyTakenException();
        }
        GameData gameData = new GameData(nextGameID++, null, null, gameName, chessGame);
        nameToIDMap.put(gameData.gameName(), gameData.gameID());
        gameDataStore.put(gameData.gameID(), gameData);
        return gameData.gameID();
    }

    @Override
    public GameData getGameByName(String gameName) {
        Integer gameID = nameToIDMap.get(gameName);
        return gameID != null ? gameDataStore.get(gameID) : null;
    }

    @Override
    public GameData getGameByID(int gameID) {
        return gameDataStore.get(gameID);
    }

    @Override
    public synchronized void updatePlayerNames(int gameID, String whitePlayerName, String blackPlayerName) {
        if (whitePlayerName != null && userDataStore.getUserData(whitePlayerName) == null) {
            throw new IllegalArgumentException("White player not found");
        }
        if (blackPlayerName != null && userDataStore.getUserData(blackPlayerName) == null) {
            throw new IllegalArgumentException("black player not found");
        }
        GameData gameData = gameDataStore.get(gameID);
        GameData updatedData = new GameData(gameData.gameID(), whitePlayerName, blackPlayerName,
                                            gameData.gameName(), gameData.game());
        gameDataStore.put(gameID, updatedData);
    }

    @Override
    public List<GameData> getAllGames() {
        return new ArrayList<>(gameDataStore.values());
    }

    @Override
    public void deleteGame(int gameID) {
        if (gameDataStore.containsKey(gameID)) {
            GameData gameData = gameDataStore.get(gameID);
            nameToIDMap.remove(gameData.gameName());
            gameDataStore.remove(gameID);
        }
    }

    @Override
    public void clearDatabase() {
        gameDataStore.clear();
        nameToIDMap.clear();
    }

}
