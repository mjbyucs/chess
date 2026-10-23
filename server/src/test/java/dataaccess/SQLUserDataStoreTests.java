package dataaccess;

import dataaccess.sql.SQLUserDataStore;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SQLUserDataStoreTests {

    private static final String DEFAULT_USER_NAME = "bob";
    private SQLUserDataStore userDAO;
    private SQLTestHelper sqlTestHelper;
    private final UserData defaultUser = new UserData(DEFAULT_USER_NAME, "marley", "peace@gmail.com");

    @BeforeEach
    void setUp() {
        sqlTestHelper = new SQLTestHelper();
        DatabaseManager.createDatabase();
        userDAO = new SQLUserDataStore(sqlTestHelper::getConnection);
    }

    @AfterEach
    void tearDown() {
        sqlTestHelper.disableConnectionFailures();
        userDAO.clearDatabase();
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
    void retrieveUserAfterClearingDatabase() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        userDAO.clearDatabase();
        Assertions.assertNull(userDAO.getUserData(DEFAULT_USER_NAME));
    }

    @Test
    void sqlExceptionOnAddUser() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.addUserData(defaultUser)
        );
    }

    @Test
    void sqlExceptionOnGetUserData() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.getUserData(defaultUser.username())
        );
    }

    @Test
    void sqlExceptionOnGetIdForUsername() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.getIdForUsername(defaultUser.username())
        );
    }

    @Test
    void sqlExceptionOnDeleteUserData() {
        userDAO.addUserData(defaultUser);
        Assertions.assertEquals(defaultUser, userDAO.getUserData(DEFAULT_USER_NAME));
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDAO.deleteUserData(defaultUser)
        );
    }
}
