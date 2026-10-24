package dataaccess;

import dataaccess.sql.SQLUserDataStore;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SQLUserDataStoreTests extends AbstractUserDataStoreTests {

    private static final String DEFAULT_USER_NAME = "bob";
    private SQLUserDataStore userDataStore;
    private SQLTestHelper sqlTestHelper;
    private final UserData defaultUser = new UserData(DEFAULT_USER_NAME, "marley", "peace@gmail.com");

    @Override
    protected UserDAO createUserDAO() {
        sqlTestHelper = new SQLTestHelper();
        DatabaseManager.createDatabase();
        userDataStore = new SQLUserDataStore(sqlTestHelper::getConnection);
        return userDataStore;
    }

    @Override
    protected void tearDownDAO() {
        sqlTestHelper.disableConnectionFailures();
        userDAO.clearDatabase();
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
                () -> userDataStore.getIdForUsername(defaultUser.username())
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

    @Test
    void sqlExceptionOnClearDatabase() {
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> userDataStore.clearDatabase()
        );
    }
}
