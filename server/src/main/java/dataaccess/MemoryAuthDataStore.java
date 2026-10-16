package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDataStore implements AuthDAO {
    private Map<String, String> authDataStore;

    public MemoryAuthDataStore() {
        resetDatabase();
    }

    @Override
    public void addAuthData(AuthData authData) {
        authDataStore.put(authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuthData(String authToken) {
        String userName = authDataStore.get(authToken);
        return userName == null ? null : new AuthData(authToken, userName);
    }

    @Override
    public void deleteAuthData(AuthData authData) {
        authDataStore.remove(authData.authToken());
    }

    @Override
    public void deleteDatabase() {
        resetDatabase();
    }

    private void resetDatabase() {
        authDataStore = new HashMap<>();
    }
}
