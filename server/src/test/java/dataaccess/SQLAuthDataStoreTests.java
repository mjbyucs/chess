package dataaccess;

import dataaccess.sql.SQLAuthDataStore;
import dataaccess.sql.SQLUserDataStore;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SQLAuthDataStoreTests {

    private static final String DEFAULT_AUTH_TOKEN = "some_auth";
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private SQLTestHelper sqlTestHelper;
    private final AuthData defaultAuth = new AuthData(DEFAULT_AUTH_TOKEN, "user");

    @BeforeEach
    void setUp() {
        sqlTestHelper = new SQLTestHelper();
        DatabaseManager.createDatabase();
        SQLUserDataStore userDataStore = new SQLUserDataStore(DatabaseManager::getConnection);
        userDataStore.addUserData(new UserData(defaultAuth.username(), "", ""));
        authDAO = new SQLAuthDataStore(sqlTestHelper::getConnection, userDataStore);
        userDAO = userDataStore;
    }

    @AfterEach
    void tearDown() {
        sqlTestHelper.disableConnectionFailures();
        authDAO.clearDatabase();
        userDAO.clearDatabase();
    }

    @Test
    void addAndGetAuthData() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }

    @Test
    void nonexistentAuth() {
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }

    @Test
    void retrieveAuthAfterDeletingAuth() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
        authDAO.deleteAuthData(defaultAuth);
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }

    @Test
    void retrieveAuthAfterClearingTable() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
        authDAO.clearDatabase();
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
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
}
