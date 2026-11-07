package model;

import chess.ChessGame;

public class SessionState {
    public enum UserStatus {
        SIGNED_IN,
        SIGNED_OUT,
    }

    private UserStatus userStatus;
    private AuthData authData;
    private int serverGameId = -1;
    ChessGame.TeamColor perspective;
    GameList gameList = new GameList();
    GameData gameData;

    public SessionState() {
        signOut();      // initial state
    }

    public boolean isUserLoggedIn() {
        return userStatus == UserStatus.SIGNED_IN;
    }

    private void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public AuthData getAuthData() {
        return authData;
    }

    public void signIn(AuthData authData) {
        this.authData = authData;
        setUserStatus(UserStatus.SIGNED_IN);
    }

    public void signOut() {
        this.authData = null;
        this.serverGameId = -1;
        this.perspective = null;
        this.gameData = null;
        setUserStatus(UserStatus.SIGNED_OUT);
    }

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public boolean alreadyInGame(GameListEntry gameEntry, ChessGame.TeamColor requestedColor) {
        if (authData != null && gameEntry != null) {
            return requestedColor == ChessGame.TeamColor.WHITE ?
                    authData.username().equals(gameEntry.whiteUsername()) :
                    authData.username().equals(gameEntry.blackUsername());
        }
        return false;
    }

    public void registerGameIdAndPerspective(int serverGameId, ChessGame.TeamColor perspective) {
        this.serverGameId = serverGameId;
        this.perspective = perspective;
    }

    public int getLocalGameNumber() {
        return mapServerGameIdToLocalId(serverGameId);
    }

    public ChessGame.TeamColor getPerspective() {
        return perspective;
    }

    public void setGameList(GameList gameList) {
        this.gameList = gameList;
    }

    public GameListEntry getGameEntryByInternalId(int localGameId) {
        return localGameId >= 0 && localGameId < gameList.size() ? gameList.get(localGameId) : null;
    }

    public GameListEntry getGameEntryByServerId(int serverGameId) {
        return getGameEntryByInternalId(mapServerGameIdToLocalId(serverGameId));
    }

    public int mapServerGameIdToLocalId(Integer serverGameId) {
        for (int i = 0; i < gameList.size(); i++) {
            if (serverGameId == gameList.get(i).gameID()) {
                return i;
            }
        }
        return -1;
    }

    public Integer mapLocalGameIdToServerId(int gameId) {
        return gameId >= 0 && gameId < gameList.size() ? gameList.get(gameId).gameID() : null;
    }

}
