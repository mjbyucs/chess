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

    }

    @Override
    public UserData getUserData(String userName) {
        return null;
    }

    @Override
    public void deleteUserData(UserData userData) {

    }

    @Override
    public void deleteDatabase() {

    }

    private void resetDatabase() {
        userDataStore = new HashMap<>();
    }
}
