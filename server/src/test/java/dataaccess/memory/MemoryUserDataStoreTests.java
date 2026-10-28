package dataaccess.memory;

import dataaccess.AbstractUserDataStoreTests;
import dataaccess.UserDAO;


public class MemoryUserDataStoreTests extends AbstractUserDataStoreTests {

    @Override
    protected UserDAO createUserDAO() {
        return new MemoryUserDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
