package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.GameDAO;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.AuthorizationRequest;
import model.GameData;

import java.util.List;

public class GameService {
    private final Object lock = new Object();
    private final GameDAO gameDAO;
    private final AuthService authService;

    public GameService(GameDAO gameDAO, AuthService authService) {
        this.gameDAO = gameDAO;
        this.authService = authService;
    }

    public int createGame(AuthorizationRequest authorizationRequest, String gameName) throws UnauthorizedException, AlreadyTakenException {
        AuthData authData = authService.getAuthData(authorizationRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException();
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new IllegalArgumentException("Invalid game name");
        }

        return gameDAO.addGame(gameName, new ChessGame());
    }

    protected GameData getGame(int gameID) {
        return gameDAO.getGameByID(gameID);
    }

    public void joinGame(AuthorizationRequest authorizationRequest, int gameID, String playerColor)
            throws UnauthorizedException, AlreadyTakenException {
        AuthData authData = authService.getAuthData(authorizationRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException();
        }

        boolean isWhite;
        try {
            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
            isWhite = ChessGame.TeamColor.WHITE == color;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid playerColor");
        }

        GameData gameData = getGame(gameID);
        if (gameData == null) {
            throw new IllegalArgumentException("Invalid gameID");
        }
        synchronized (lock) {
            validatePlayerAvailable(isWhite ? gameData.whiteUsername() : gameData.blackUsername());
            gameDAO.updatePlayerNames(gameData.gameID(),
                    isWhite ? authData.username() : gameData.whiteUsername(),
                    isWhite ? gameData.blackUsername() : authData.username());
        }
    }

    private void validatePlayerAvailable(String playerName) throws AlreadyTakenException {
        if (playerName != null) {
            throw new AlreadyTakenException();
        }
    }

    public List<GameData> listGames(AuthorizationRequest authorizationRequest) throws UnauthorizedException {
        AuthData authData = authService.getAuthData(authorizationRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException();
        }
        return gameDAO.getAllGames();
    }
}
