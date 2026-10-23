package service;

import dataaccess.*;
import dataaccess.memory.MemoryAuthDataStore;
import dataaccess.memory.MemoryGameDataStore;
import model.AuthorizationRequest;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameServiceTests {
    private static final String DEFAULT_USER = "The User";
    private AuthorizationRequest authorizationRequest;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        GameDAO gameDAO = new MemoryGameDataStore();
        AuthService authService = new AuthService(new MemoryAuthDataStore());
        authorizationRequest = new AuthorizationRequest(authService.createAuth(DEFAULT_USER).authToken());
        gameService = new GameService(gameDAO, authService);
    }

    @Test
    void createGameSuccessful() throws UnauthorizedException, AlreadyTakenException {
        String gameName = "my game";
        int gameID = gameService.createGame(authorizationRequest, gameName);
        GameData gameData = gameService.getGame(gameID);
        Assertions.assertEquals(gameID, gameData.gameID());
        Assertions.assertEquals(gameName, gameData.gameName());
    }

    @Test
    void createGameUnauthorized() {
        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> gameService.createGame(new AuthorizationRequest("foo"), "game")
        );
    }

    @Test
    void createGameInvalidGameName() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameService.createGame(authorizationRequest, null)
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameService.createGame(authorizationRequest, "")
        );
    }

    @Test
    void createGameDuplicateGameName() throws UnauthorizedException, AlreadyTakenException {
        String gameName = "my game";
        gameService.createGame(authorizationRequest, gameName);
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> gameService.createGame(authorizationRequest, gameName)
        );
    }

    @Test
    void joinGameSuccessfulWhite() throws UnauthorizedException, AlreadyTakenException {
        joinGameTestHelper("white");
    }

    @Test
    void joinGameSuccessfulBlack() throws UnauthorizedException, AlreadyTakenException {
        joinGameTestHelper("black");
    }

    void joinGameTestHelper(String playerColor) throws UnauthorizedException, AlreadyTakenException {
        String gameName = "my game";
        int gameID = gameService.createGame(authorizationRequest, gameName);
        GameData gameData = gameService.getGame(gameID);
        Assertions.assertNull(gameData.whiteUsername());
        Assertions.assertNull(gameData.blackUsername());

        gameService.joinGame(authorizationRequest, gameID, playerColor);
        gameData = gameService.getGame(gameID);
        if ("white".equals(playerColor)) {
            Assertions.assertEquals(DEFAULT_USER, gameData.whiteUsername());
            Assertions.assertNull(gameData.blackUsername());
        }
        else {
            Assertions.assertNull(gameData.whiteUsername());
            Assertions.assertEquals(DEFAULT_USER, gameData.blackUsername());
        }
    }

    @Test
    void joinGameUnauthorized() {
        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> gameService.joinGame(new AuthorizationRequest("foo"), 1, "white")
        );
    }

    @Test
    void joinGameInvalidColor() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameService.joinGame(authorizationRequest, 1, "blue")
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameService.joinGame(authorizationRequest, 1, null)
        );
    }

    @Test
    void joinGameInvalidGameID() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameService.joinGame(authorizationRequest, 1, "white")
        );
    }


    @Test
    void joinGameNotAvailable() throws UnauthorizedException, AlreadyTakenException {
        String gameName = "my game";
        String playerColor = "black";
        int gameID = gameService.createGame(authorizationRequest, gameName);
        gameService.joinGame(authorizationRequest, gameID, playerColor);
        Assertions.assertThrows(
                AlreadyTakenException.class,
                () -> gameService.joinGame(authorizationRequest, gameID, playerColor)
        );
    }

    @Test
    void listGamesSuccessful() throws UnauthorizedException, AlreadyTakenException {
        String gameName = "my game";
        Assertions.assertTrue(gameService.listGames(authorizationRequest).isEmpty());
        gameService.createGame(authorizationRequest, gameName + "1");
        gameService.createGame(authorizationRequest, gameName + "2");
        gameService.createGame(authorizationRequest, gameName + "3");
        List<GameData> gameDataList = gameService.listGames(authorizationRequest);
        Assertions.assertEquals(3, gameDataList.size());
        Set<String> expectedNames = Set.of(gameName + "1", gameName + "2", gameName + "3");
        Set<String> retrievedGameNames = gameDataList.stream()
                                                    .map(GameData::gameName)
                                                    .collect(Collectors.toSet());
        Assertions.assertEquals(expectedNames, retrievedGameNames);
    }

    @Test
    void listGamesUnauthorized() {
        Assertions.assertThrows(
                UnauthorizedException.class,
                () -> gameService.listGames(new AuthorizationRequest("foo"))
        );
    }

}
