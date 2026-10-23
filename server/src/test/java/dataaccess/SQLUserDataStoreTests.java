package dataaccess;

import dataaccess.sql.SQLUserDataStore;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class SQLUserDataStoreTests {

    private static final String DEFAULT_USER_NAME = "bob";
    private UserDAO userDAO;
    private final UserData defaultUser = new UserData(DEFAULT_USER_NAME, "marley", "peace@gmail.com");

    @BeforeEach
    void setUp() {
        DatabaseManager.createDatabase();
        userDAO = new SQLUserDataStore(DatabaseManager::getConnection);
    }

    @AfterEach
    void tearDown() {
        userDAO.deleteDatabase();
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
    void sqlExceptionOnAddUser() throws SQLException {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));

        userDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.addUserData(defaultUser)
        );
    }

    @Test
    void sqlExceptionOnGetAuth() throws SQLException {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));

        userDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.getUserData(defaultUser.username())
        );
    }

    @Test
    void sqlExceptionOnDeleteAuth() throws SQLException {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));

        userDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.deleteUserData(defaultUser)
        );
    }
}
