package dataaccess;

import dataaccess.memory.MemoryUserDataStore;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MemoryUserDataStoreTests extends AbstractUserDataStoreTests {

    @Override
    protected UserDAO createUserDAO() {
        return new MemoryUserDataStore();
    }

    @Override
    protected void tearDownDAO() { }
}
