package model;

import chess.ChessGame;

public class SessionState {
    public enum UserStatus {
        SIGNED_IN,
        SIGNED_OUT,
    }

    private UserStatus userStatus;
    private AuthData authData;
    private GameData gameData;
    ChessGame.TeamColor perspective = ChessGame.TeamColor.WHITE;

    public boolean isUserLoggedIn() {
        return userStatus == UserStatus.SIGNED_IN;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public AuthData getAuthData() {
        return authData;
    }

    public void setAuthData(AuthData authData) {
        this.authData = authData;
        setUserStatus(UserStatus.SIGNED_IN);
    }

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public ChessGame.TeamColor getPerspective() {
        return perspective;
    }

    public void setPerspective(ChessGame.TeamColor perspective) {
        this.perspective = perspective;
    }
}
