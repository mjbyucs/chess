package dataaccess;

import dataaccess.memory.MemoryAuthDataStore;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MemoryAuthDataStoreTests extends AbstractAuthDataStoreTests {

    @Override
    protected AuthDAO createAuthDAO() {
        return new MemoryAuthDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
