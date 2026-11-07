package client;

import chess.ChessGame;
import model.AuthData;
import model.GameList;
import model.GameListEntry;
import model.UserData;
import org.junit.jupiter.api.*;
import server.DependencyComposer;
import server.Server;

import java.util.HashMap;
import java.util.Map;


public class ServerFacadeTests {
    private static final String USER_NAME = "Gamer";
    private static final String PASSWORD = "gPW";
    private static final String EMAIL = "g4m3r@byu.edu";

    private static Server server;
    private static ServerFacade facade;
    private static UserData userData;

    @BeforeAll
    public static void init() {
        DependencyComposer dependencyComposer = new DependencyComposer(DependencyComposer.StorageType.MEMORY,
                null);
        server = new Server(dependencyComposer);
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

    @Test
    public void createGame() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Integer gameId = facade.createGame(authData, "game1");
        Assertions.assertNotNull(gameId);
        Assertions.assertTrue(gameId > 0);
    }

    @Test
    public void gameNameInvalid() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.createGame(authData, "")
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void duplicateGame() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        facade.createGame(authData, "game1");
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.createGame(authData, "game1")
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void unauthorizedGameCreation() {
        AuthData authData = new AuthData("a token", USER_NAME);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.createGame(authData, "")
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void joinGame() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Integer gameId = facade.createGame(authData, "game1");
        Assertions.assertDoesNotThrow(
                () -> facade.joinGame(authData, ChessGame.TeamColor.WHITE.name(), gameId)
        );
    }

    @Test
    public void joinGameUnauthorizedPlayer() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Integer gameId = facade.createGame(authData, "game1");
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.joinGame(new AuthData("noauth", ""),
                        ChessGame.TeamColor.WHITE.name(), gameId)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void joinGameDuplicatePlayer() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Integer gameId = facade.createGame(authData, "game1");
        facade.joinGame(authData, ChessGame.TeamColor.WHITE.name(), gameId);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.joinGame(authData, ChessGame.TeamColor.WHITE.name(), gameId)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void joinInvalidGame() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.joinGame(authData, ChessGame.TeamColor.WHITE.name(), 1)
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    public void listGames() throws ResponseException {
        AuthData authData = facade.registerUser(userData);
        Map<Integer, String> gameMap = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Integer gameID = facade.createGame(authData, "game" + i);
            gameMap.put(gameID, "game" + i);
        }
        GameList gameList = facade.listGames(authData);
        Assertions.assertEquals(3, gameList.size());
        for (GameListEntry gameItem : gameList) {
            Assertions.assertTrue(gameMap.containsKey(gameItem.gameID()));
            Assertions.assertEquals(gameMap.get(gameItem.gameID()), gameItem.gameName());
            Assertions.assertNull(gameItem.whiteUsername());
            Assertions.assertNull(gameItem.blackUsername());
        }

        AuthData anotherUser = facade.registerUser(new UserData("Mary", "mj", "mj@byu.edu"));
        facade.joinGame(anotherUser, ChessGame.TeamColor.WHITE.name(), gameList.getFirst().gameID());
        facade.joinGame(authData, ChessGame.TeamColor.BLACK.name(), gameList.getFirst().gameID());

        gameList = facade.listGames(authData);
        Assertions.assertEquals("Mary", gameList.getFirst().whiteUsername());
        Assertions.assertEquals(USER_NAME, gameList.getFirst().blackUsername());
    }


    @Test
    public void listGamesUnauthorized() {
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> facade.listGames(new AuthData("noauth", ""))
        );
        Assertions.assertEquals(ResponseException.Code.ClientError, exception.code());
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void serverNotAvailable() {
        ServerFacade localFacade = new ServerFacade(8000);
        ResponseException exception = Assertions.assertThrows(
                ResponseException.class,
                () -> localFacade.registerUser(userData)
        );
        Assertions.assertEquals(ResponseException.Code.ServerError, exception.code());
        Assertions.assertEquals("Unable to connect to the server", exception.getMessage());
    }
}
