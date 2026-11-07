package client;

import chess.ChessGame;
import model.*;

public class SessionService {
    private final SessionState sessionState;
    private final ServerFacade serverFacade;

    public SessionService(SessionState sessionState, ServerFacade serverFacade) {
        this.sessionState = sessionState;
        this.serverFacade = serverFacade;
    }

    public boolean isUserLoggedIn() {
        return sessionState.isUserLoggedIn();
    }

    public GameData getGameData() {
        return sessionState.getGameData();
    }

    public int getLocalGameNumber() { return sessionState.getLocalGameNumber(); }

    public ChessGame.TeamColor getPerspective() {
        return sessionState.getPerspective();
    }

    public void registerUser(String username, String password, String email) throws ResponseException {
        AuthData authData = serverFacade.registerUser(new UserData(username, password, email));
        sessionState.signIn(authData);
    }

    public void loginUser(String username, String password) throws ResponseException {
        AuthData authData = serverFacade.loginUser(new UserData(username, password, ""));
        sessionState.signIn(authData);
    }

    public void logoutUser() throws ResponseException {
        if (sessionState.isUserLoggedIn()) {
            AuthData authData = sessionState.getAuthData();
            // ensure we are signed out locally regardless
            sessionState.signOut();
            try {
                serverFacade.logoutUser(authData);
            }
            catch (ResponseException ex) {
                if (ex.code() == ResponseException.Code.ServerError) {
                    throw new ResponseException(ResponseException.Code.ServerError,
                            "Logged out locally, but server logout failed.");
                }
            }
        }
    }

    public int createGame(String gameName) throws ResponseException {
        Integer gameId = serverFacade.createGame(sessionState.getAuthData(), gameName);
        if (gameId != null) {
            listGames();
            int internalId = sessionState.mapServerGameIdToLocalId(gameId);
            if (internalId >= 0) {
                return internalId;
            }
        }
        throw new ResponseException(400, "Game wasn't successfully created and registered on the server");
    }

    public GameList listGames() throws ResponseException {
        GameList gameList = serverFacade.listGames(sessionState.getAuthData());
        sessionState.setGameList(gameList);
        return gameList;
    }

    public void joinGame(int gameId, String playerColor) throws ResponseException {
        int serverGameId = getServerGameIdFromLocalId(gameId);
        ChessGame.TeamColor requestedColor = ChessGame.TeamColor.valueOf(playerColor);
        if (!sessionState.alreadyInGame(serverGameId, requestedColor)) {
            serverFacade.joinGame(sessionState.getAuthData(), playerColor, serverGameId);
            listGames();        // update the list since I've now registered
        }
        watchGame(serverGameId, requestedColor);
    }

    public void observeGame(int gameId) throws ResponseException {
        int serverGameId = getServerGameIdFromLocalId(gameId);
        watchGame(serverGameId, ChessGame.TeamColor.WHITE);
    }

    private void watchGame(int serverGameId, ChessGame.TeamColor perspective) {
        // This method will be set up to watch for updates to the chess board for the given game
        // for now we fake it by creating an empty board and registering it with the session state
        sessionState.registerGameIdAndPerspective(serverGameId, perspective);
        GameListEntry gameListEntry = sessionState.getGameEntry(serverGameId);
        if (gameListEntry == null) {
            throw new RuntimeException("We got a null game entry for server game " + serverGameId);
        }
        sessionState.setGameData(new GameData(gameListEntry.gameID(),
                                                gameListEntry.whiteUsername(),
                                                gameListEntry.blackUsername(),
                                                gameListEntry.gameName(),
                                                new ChessGame()));
    }

    private int getServerGameIdFromLocalId(int gameId) throws ResponseException {
        Integer serverGameId = sessionState.mapLocalGameIdToServerId(gameId);
        if (serverGameId == null) {
            throw new ResponseException(400, "Not a valid game ID");
        }
        return serverGameId;
    }
}
