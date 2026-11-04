package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.GameList;
import org.jetbrains.annotations.NotNull;

public interface GameDAO {

    int addGame(@NotNull String gameName, @NotNull ChessGame chessGame) throws AlreadyTakenException;

    GameData getGameByName(String gameName);

    GameData getGameByID(int gameID);

    void updatePlayerNames(int gameID, String whitePlayerName, String blackPlayerName);

    GameList getAllGames();

    void deleteGame(int gameID);

    void clearDatabase();

}

