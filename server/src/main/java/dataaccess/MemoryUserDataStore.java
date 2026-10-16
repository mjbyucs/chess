package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDataStore implements UserDAO {
    Map<String, UserData> userDataStore;

    public MemoryUserDataStore() {
        resetDatabase();
    }

    @Override
    public void addUserData(UserData userData) {
        userDataStore.put(userData.username(), userData);
    }

    @Override
    public UserData getUserData(String userName) {
        return userDataStore.get(userName);
    }

    @Override
    public void deleteUserData(UserData userData) {
        userDataStore.remove(userData.username());
    }

    @Override
    public void deleteDatabase() {
        resetDatabase();
    }

    private void resetDatabase() {
        userDataStore = new HashMap<>();
    }
}
