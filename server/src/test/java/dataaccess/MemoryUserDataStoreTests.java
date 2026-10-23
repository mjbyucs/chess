package dataaccess;

import dataaccess.memory.MemoryUserDataStore;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MemoryUserDataStoreTests {

    private static final String DEFAULT_USER_NAME = "bob";
    private UserDAO userDAO;
    private final UserData defaultUser = new UserData(DEFAULT_USER_NAME, "marley", "peace@gmail.com");

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDataStore();
    }

    @Test
    void addAndGetUserData() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
    }

    @Test
    void nonexistentUser() {
        Assertions.assertNull(userDAO.getUserData(DEFAULT_USER_NAME));
    }

    @Test
    void retrieveUserAfterDeletingUser() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        userDAO.deleteUserData(defaultUser);
        Assertions.assertNull(userDAO.getUserData(DEFAULT_USER_NAME));
    }

    @Test
    void retrieveUserAfterDeletingDatabase() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        userDAO.clearDatabase();
        Assertions.assertNull(userDAO.getUserData(DEFAULT_USER_NAME));
    }
}
