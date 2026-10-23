package dataaccess;

import dataaccess.memory.MemoryUserDataStore;


public class MemoryUserDataStoreTests extends AbstractUserDataStoreTests {

    @Override
    protected UserDAO createUserDAO() {
        return new MemoryUserDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
