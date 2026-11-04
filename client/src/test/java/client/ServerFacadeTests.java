package client;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {
    private static final String USER_NAME = "Gamer";
    private static final String PASSWORD = "gPW";
    private static final String EMAIL = "g4m3r@byu.edu";

    private static Server server;
    private static ServerFacade facade;
    private static UserData userData;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        userData = new UserData(USER_NAME, PASSWORD, EMAIL);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        try {
            facade.deleteDatabase();
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void registrationSuccessful() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Assertions.assertEquals(userData.username(), authData.username());
        Assertions.assertNotNull(authData.authToken());
    }

    @Test
    public void registrationDuplicated() throws ResponseException {
        facade.registerUser(userData);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.registerUser(userData)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void registrationDataIsInvalid() {
        UserData badUserData = new UserData("", "", "");
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.registerUser(badUserData)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void loginAndLogoutSuccessful() throws ResponseException {
        facade.registerUser(userData);
        AuthData authData = facade.loginUser(userData);
        Assertions.assertEquals(userData.username(), authData.username());

        Assertions.assertDoesNotThrow(
                () -> facade.logoutUser(authData)
        );
        // confirm logout by trying again - should fail
        Assertions.assertThrows(
                ResponseException.class,
                () -> facade.logoutUser(authData)
        );
    }

    @Test
    public void loginDataIsInvalid() {
        UserData badUserData = new UserData("", "", "");
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.registerUser(badUserData)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void loginUnregisteredUser() {
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.loginUser(userData)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void loginWrongPassword() throws ResponseException {
        facade.registerUser(new UserData(USER_NAME, "alternate", EMAIL));
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.loginUser(userData)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

//    @Test
//    public void logoutSuccessful() {
//        AuthData authData = facade.loginUser(userData);
//        Assertions.assertEquals(userData.username(), authData.username());
//    }
}
