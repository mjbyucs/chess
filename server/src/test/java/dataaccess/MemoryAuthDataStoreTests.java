package dataaccess;

import dataaccess.memory.MemoryAuthDataStore;


public class MemoryAuthDataStoreTests extends AbstractAuthDataStoreTests {

    @Override
    protected AuthDAO createAuthDAO() {
        return new MemoryAuthDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
