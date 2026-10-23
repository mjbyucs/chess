package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class AbstractAuthDataStoreTests {

    private static final String DEFAULT_AUTH_TOKEN = "some_auth";
    protected AuthDAO authDAO;
    private final AuthData defaultAuth = new AuthData(DEFAULT_AUTH_TOKEN, "user");

    protected abstract AuthDAO createAuthDAO();
    protected abstract void tearDownDAO();

    @BeforeEach
    void setUp() {
        authDAO = createAuthDAO();
    }

    @AfterEach
    void tearDown() {
        tearDownDAO();
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
        authDAO.clearDatabase();
        Assertions.assertNull(authDAO.getAuthData(DEFAULT_AUTH_TOKEN));
    }
}
