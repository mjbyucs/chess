package dataaccess.memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDataStore implements AuthDAO {
    private final Map<String, String> authDataStore = new HashMap<>();

    public MemoryAuthDataStore() {
        clearDatabase();
    }

    @Override
    public synchronized void addAuthData(AuthData authData) {
        authDataStore.put(authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuthData(String authToken) {
        String userName = authDataStore.get(authToken);
        return userName == null ? null : new AuthData(authToken, userName);
    }

    @Override
    public synchronized void deleteAuthData(AuthData authData) {
        if (!authDataStore.containsKey(authData.authToken())) {
            throw new DataAccessException("Invalid authToken: [" + authData.authToken() + "]");
        }
        authDataStore.remove(authData.authToken());
    }

    @Override
    public void clearDatabase() {
        authDataStore.clear();
    }

}
