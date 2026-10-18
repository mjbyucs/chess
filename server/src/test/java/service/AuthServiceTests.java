package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataStore;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthServiceTests {
    private static final String USER_NAME = "fred";
    private AuthDAO authDAO;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDataStore();
        authService = new AuthService(authDAO);
    }

    @Test
    void createAuthData() {
        AuthData authData = authService.createAuth(USER_NAME);
        Assertions.assertEquals(USER_NAME, authData.username());
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertNotEquals("", authData.authToken());
    }

    @Test
    void getAuthData() {
        AuthData authData = authService.createAuth(USER_NAME);
        Assertions.assertEquals(authData, authService.getAuthData(authData.authToken()));
    }

    @Test
    void getAuthDataAfterDeletion() {
        AuthData authData = authService.createAuth(USER_NAME);
        authDAO.deleteDatabase();
        Assertions.assertNull(authService.getAuthData(authData.authToken()));
    }

    @Test
    void deleteAuthData() {
        AuthData authData = authService.createAuth(USER_NAME);
        authDAO.deleteAuthData(authData);
        Assertions.assertNull(authService.getAuthData(authData.authToken()));
    }

    @Test
    void deleteAuthDataWithBadToken() {
        Assertions.assertThrows(
                DataAccessException.class,
                () -> authService.deleteAuthData(new AuthData("bad", "user"))
        );
    }
}
