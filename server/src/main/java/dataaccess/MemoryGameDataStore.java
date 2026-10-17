package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDataStore implements GameDAO {
    private Map<Integer, GameData> gameDataStore;
    private Map<String, Integer> nameToIDMap;

    public MemoryGameDataStore() {
        resetDatabase();
    }

    @Override
    public void createGame(GameData gameData) throws AlreadyTakenException {
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
    public void updateGameData(GameData gameData) {
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
        gameDataStore = new HashMap<>();
        nameToIDMap = new HashMap<>();
    }
}
