package dataaccess.memory;

import dataaccess.AbstractAuthDataStoreTests;
import dataaccess.AuthDAO;


public class MemoryAuthDataStoreTests extends AbstractAuthDataStoreTests {

    @Override
    protected AuthDAO createAuthDAO() {
        return new MemoryAuthDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
