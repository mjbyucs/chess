package dataaccess;

import dataaccess.memory.MemoryAuthDataStore;
import dataaccess.sql.SQLAuthDataStore;
import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLAuthDataStoreTests {

    private static final String DEFAULT_AUTH_TOKEN = "some_auth";
    private AuthDAO authDAO;
    private final AuthData defaultAuth = new AuthData(DEFAULT_AUTH_TOKEN, "user");

    @BeforeEach
    void setUp() {
        DatabaseManager.createDatabase();
        authDAO = new SQLAuthDataStore(DatabaseManager::getConnection);
    }

    @AfterEach
    void tearDown() {
        authDAO.deleteDatabase();
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
    void retrieveAuthAfterDeletion() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
        authDAO.deleteAuthData(defaultAuth);
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }

    @Test
    void sqlExceptionOnAddAuth() throws SQLException {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        authDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.addAuthData(defaultAuth)
        );

    }

    @Test
    void sqlExceptionOnGetAuth() throws SQLException {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        authDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.getAuthData(defaultAuth.authToken())
        );

    }

    @Test
    void sqlExceptionOnDeleteAuth() throws SQLException {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));

        authDAO.deleteDatabase();
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authDAO.deleteAuthData(defaultAuth)
        );

    }
}
