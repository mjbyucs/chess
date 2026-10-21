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
    private static final String USER_NAME = "Fred_Brown23";
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
    void registerNoRequest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(null)
        );
    }

    @Test
    void registerInvalidUserName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest("", "passwd", USER_NAME + "@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        "B%b", "passwd", USER_NAME + "@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        "Name with space", "passwd", USER_NAME + "@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        "A_username_that_is_far_far_too_long",
                        "passwd", USER_NAME + "@byu.edu"))
        );
    }

    @Test
    void registerInvalidPassword() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "", USER_NAME + "@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "with\u0007bell", USER_NAME + "@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME,
                        "A_really_long_password_that_exceeds_the_limit",
                        USER_NAME + "@byu.edu"))
        );
    }

    @Test
    void registerInvalidEmail() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "passwd", ""))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "passwd", null))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "passwd", USER_NAME + "@33@byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "passwd", "bad_host@host_byu.edu"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest(
                        USER_NAME, "passwd", "bad_domain@byu.edu0"))
        );
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
    void loginUnsuccessfulWrongPassword() throws AlreadyTakenException {
        String password = "passwd";
        createRegistration(password);
        LoginRequest loginRequest = new LoginRequest(USER_NAME, "not_the_password");

        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> userService.login(loginRequest)
        );
    }

    @Test
    void loginInvalidParameters() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(null)
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(new LoginRequest(null, "passwd"))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(new LoginRequest(USER_NAME, ""))
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(new LoginRequest(USER_NAME,  null))
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
