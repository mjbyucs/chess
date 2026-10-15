package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class MemoryGameDataStore implements GameDAO {
    private Map<String, String> gameDataStore;

    public MemoryGameDataStore() {
        resetDatabase();
    }


    @Override
    public void deleteDatabase() {
        resetDatabase();
    }

    private void resetDatabase() {
        gameDataStore = new HashMap<>();
    }
}
