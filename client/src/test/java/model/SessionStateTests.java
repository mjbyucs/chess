package model;

import chess.ChessGame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SessionStateTests {

    private static AuthData authData;
    private static GameList gameList;
    private SessionState sessionState;

    @BeforeAll
    static void globalSetup() {
        String username = "A user";
        authData = new AuthData("A token", username);
        gameList = new GameList();
        gameList.add(new GameListEntry(1001, null, null, "First"));
        gameList.add(new GameListEntry(1002, "White", "Black", "Second"));
        gameList.add(new GameListEntry(1003, null, username, "Third"));
    }

    @BeforeEach
    void setup() {
        sessionState = new SessionState();
    }

    @Test
    public void defaults() {
        Assertions.assertFalse(sessionState.isUserLoggedIn());
        Assertions.assertNull(sessionState.getAuthData());
        Assertions.assertNull(sessionState.getAuthData());
        Assertions.assertEquals(-1, sessionState.getLocalGameNumber());
        Assertions.assertNull(sessionState.getPerspective());
    }

    @Test
    public void signIn() {
        sessionState.signIn(authData);
        Assertions.assertTrue(sessionState.isUserLoggedIn());
        Assertions.assertEquals(authData, sessionState.getAuthData());
    }

    @Test
    public void signOut() {
        // fully sign in and then sign out to make sure all state is cleared
        sessionState.signIn(authData);
        sessionState.setGameList(gameList);
        sessionState.registerGameIdAndPerspective(1001, ChessGame.TeamColor.WHITE);
        sessionState.signOut();
        Assertions.assertFalse(sessionState.isUserLoggedIn());
        Assertions.assertNull(sessionState.getAuthData());
        Assertions.assertNull(sessionState.getGameData());
        Assertions.assertNull(sessionState.getPerspective());
        Assertions.assertEquals(-1, sessionState.getLocalGameNumber());
    }

    @Test
    public void gamePerspective() {
        Assertions.assertNull(sessionState.getGameData());
        sessionState.setGameList(gameList);
        sessionState.registerGameIdAndPerspective(1002, ChessGame.TeamColor.WHITE);
        Assertions.assertEquals(1, sessionState.getLocalGameNumber());
        Assertions.assertEquals(ChessGame.TeamColor.WHITE, sessionState.getPerspective());
    }

    @Test
    public void gameEntries() {
        Assertions.assertNull(sessionState.getGameEntryByInternalId(1));
        Assertions.assertNull(sessionState.getGameEntryByInternalId(-1));
        sessionState.setGameList(gameList);
        Assertions.assertEquals(gameList.getFirst(), sessionState.getGameEntryByServerId(1001));
    }

    @Test
    public void alreadyInGameTrue() {
        sessionState.signIn(authData);
        sessionState.setGameList(gameList);
        Assertions.assertTrue(sessionState.alreadyInGame(gameList.get(2), ChessGame.TeamColor.BLACK));
    }

    @Test
    public void alreadyInGameFalse() {
        Assertions.assertFalse(sessionState.alreadyInGame(gameList.get(2), ChessGame.TeamColor.WHITE));
        sessionState.signIn(authData);
        Assertions.assertFalse(sessionState.alreadyInGame(gameList.get(2), ChessGame.TeamColor.WHITE));
        sessionState.setGameList(gameList);
        Assertions.assertFalse(sessionState.alreadyInGame(gameList.get(2), ChessGame.TeamColor.WHITE));
    }

    @Test
    public void serverGameIdMapping() {
        sessionState.setGameList(gameList);
        Assertions.assertEquals(0, sessionState.mapServerGameIdToLocalId(1001));
        Assertions.assertEquals(1, sessionState.mapServerGameIdToLocalId(1002));
        Assertions.assertEquals(2, sessionState.mapServerGameIdToLocalId(1003));
        Assertions.assertEquals(-1, sessionState.mapServerGameIdToLocalId(1004));
    }

    @Test
    public void localGameIdMapping() {
        Assertions.assertNull(sessionState.mapLocalGameIdToServerId(0));
        sessionState.setGameList(gameList);
        Assertions.assertEquals(1001, sessionState.mapLocalGameIdToServerId(0));
        Assertions.assertEquals(1002, sessionState.mapLocalGameIdToServerId(1));
        Assertions.assertEquals(1003, sessionState.mapLocalGameIdToServerId(2));
        Assertions.assertNull(sessionState.mapLocalGameIdToServerId(3));
        Assertions.assertNull(sessionState.mapLocalGameIdToServerId(-1));
    }
}
