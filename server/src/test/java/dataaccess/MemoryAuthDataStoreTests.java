package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MemoryAuthDataStoreTests {

    private static final String DEFAULT_AUTH_TOKEN = "some_auth";
    private AuthDAO authDAO;
    private final AuthData defaultAuth = new AuthData(DEFAULT_AUTH_TOKEN, "user");

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDataStore();
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
    void retrieveAuthAfterDeletingDatabase() {
        authDAO.addAuthData(defaultAuth);
        Assertions.assertEquals(defaultAuth, authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
        authDAO.deleteDatabase();
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }
}
