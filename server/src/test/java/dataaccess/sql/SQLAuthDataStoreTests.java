package dataaccess.sql;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SQLAuthDataStoreTests extends AbstractAuthDataStoreTests {

    private static final String DEFAULT_AUTH_TOKEN = "some_auth";
    private UserDAO userDAO;
    private SQLTestHelper sqlTestHelper;
    private final AuthData defaultAuth = new AuthData(DEFAULT_AUTH_TOKEN, "user");

    @Override
    protected AuthDAO createAuthDAO() {
        sqlTestHelper = new SQLTestHelper();
        DatabaseManager.createDatabase();
        SQLUserDataStore userDataStore = new SQLUserDataStore(DatabaseManager::getConnection);
        userDataStore.addUserData(new UserData(defaultAuth.username(), "", ""));
        userDAO = userDataStore;
        return new SQLAuthDataStore(sqlTestHelper::getConnection, userDataStore);
    }

    @Override
    protected void tearDownDAO() {
        sqlTestHelper.disableConnectionFailures();
        authDAO.clearDatabase();
        userDAO.clearDatabase();
    }

    @Test
    void sqlExceptionOnAddAuth() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.addAuthData(defaultAuth)
        );
    }

    @Test
    void sqlExceptionOnGetAuth() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.getAuthData(defaultAuth.authToken())
        );

    }

    @Test
    void sqlExceptionOnDeleteAuth() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.deleteAuthData(defaultAuth)
        );
    }

    @Test
    void sqlExceptionOnClearDatabase() {
        sqlTestHelper.enableConnectionFailures();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.clearDatabase()
        );
    }
}
