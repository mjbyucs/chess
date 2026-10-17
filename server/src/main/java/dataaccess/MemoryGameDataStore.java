package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDataStore implements GameDAO {
    private final Map<Integer, GameData> gameDataStore = new HashMap<>();
    private final Map<String, Integer> nameToIDMap = new HashMap<>();

    public MemoryGameDataStore() {
        resetDatabase();
    }

    @Override
    public synchronized void createGame(GameData gameData) throws AlreadyTakenException {
        if (gameDataStore.containsKey(gameData.gameID()) || nameToIDMap.containsKey(gameData.gameName())) {
            throw new AlreadyTakenException();
        }
        updateGameData(gameData);
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
    public synchronized void updateGameData(GameData gameData) {
        GameData currentGameData = gameDataStore.get(gameData.gameID());
        if (currentGameData != null && !currentGameData.gameName().equals(gameData.gameName())) {
            // game name is changing so remove the old mapping
            nameToIDMap.remove(currentGameData.gameName());
        }
        nameToIDMap.put(gameData.gameName(), gameData.gameID());
        gameDataStore.put(gameData.gameID(), gameData);
    }

    @Override
    public List<GameData> getAllGames() {
        return new ArrayList<>(gameDataStore.values());
    }

    @Override
    public void deleteDatabase() {
        resetDatabase();
    }

    private void resetDatabase() {
        gameDataStore.clear();
        nameToIDMap.clear();
    }
}
