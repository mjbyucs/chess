package dataaccess;

import dataaccess.memory.MemoryGameDataStore;
import dataaccess.memory.MemoryUserDataStore;

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
