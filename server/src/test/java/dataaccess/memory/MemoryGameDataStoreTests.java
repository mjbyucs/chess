package dataaccess.memory;

import dataaccess.AbstractGameDataStoreTests;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class MemoryGameDataStoreTests extends AbstractGameDataStoreTests {
    MemoryUserDataStore userDataStore;

    @Override
    protected UserDAO createUserDAO() {
        userDataStore = new MemoryUserDataStore();
        return userDataStore;
    }

    @Override
    protected GameDAO createGameDAO() {
        return new MemoryGameDataStore(userDataStore);
    }

    @Override
    protected void tearDownDAOs() { }
}
