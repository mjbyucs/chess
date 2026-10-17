package service;

import dataaccess.*;
import model.AuthorizationRequest;
import model.AuthorizedUserResult;
import model.LoginRequest;
import model.RegisterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTests {
    private static final String USER_NAME = "fred";
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserDAO userDAO = new MemoryUserDataStore();
        AuthService authService = new AuthService(new MemoryAuthDataStore());
        userService = new UserService(userDAO, authService);
    }

    @Test
    void registerSuccessful() throws AlreadyTakenException {
        RegisterRequest registerRequest = new RegisterRequest(USER_NAME, "passwd", USER_NAME + "@byu.edu");
        AuthorizedUserResult authorizedUserResult = userService.register(registerRequest);
        Assertions.assertEquals(USER_NAME, authorizedUserResult.username());
        Assertions.assertNotNull(authorizedUserResult.authToken());
        Assertions.assertNotEquals("", authorizedUserResult.authToken());
    }

    @Test
    void registerUnavailable() throws AlreadyTakenException {
        RegisterRequest registerRequest = new RegisterRequest(USER_NAME, "passwd", USER_NAME + "@byu.edu");
        userService.register(registerRequest);
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> userService.register(registerRequest)
        );
    }

    private void createRegistration(String password) throws AlreadyTakenException {
        RegisterRequest registerRequest = new RegisterRequest(USER_NAME, password, USER_NAME + "@byu.edu");
        userService.register(registerRequest);
    }

    @Test
    void loginSuccessful() throws AlreadyTakenException, UnauthorizedException {
        String password = "passwd";
        createRegistration(password);
        LoginRequest loginRequest = new LoginRequest(USER_NAME, password);
        AuthorizedUserResult authorizedUserResult = userService.login(loginRequest);

        Assertions.assertEquals(USER_NAME, authorizedUserResult.username());
        Assertions.assertNotNull(authorizedUserResult.authToken());
        Assertions.assertNotEquals("", authorizedUserResult.authToken());
    }

    @Test
    void loginUnsuccessfulNoRegistration() {
        String password = "passwd";
        LoginRequest loginRequest = new LoginRequest(USER_NAME, password);

        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> userService.login(loginRequest)
        );
    }

    @Test
    void loginUnsuccessfulBadPassword() throws AlreadyTakenException {
        String password = "passwd";
        createRegistration(password);
        LoginRequest loginRequest = new LoginRequest(USER_NAME, "");

        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> userService.login(loginRequest)
        );
    }

    @Test
    void logoutSuccessful() throws AlreadyTakenException, UnauthorizedException {
        String password = "passwd";
        createRegistration(password);
        LoginRequest loginRequest = new LoginRequest(USER_NAME, password);
        AuthorizedUserResult authorizedUserResult = userService.login(loginRequest);
        userService.logout(new AuthorizationRequest(authorizedUserResult.authToken()));
        // logout should remove the authorization so a second attempt should fail
        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> userService.logout(new AuthorizationRequest(authorizedUserResult.authToken()))
        );
    }

    @Test
    void logoutWithBadAuthToken() {
        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> userService.logout(new AuthorizationRequest("bad"))
        );
    }
}
