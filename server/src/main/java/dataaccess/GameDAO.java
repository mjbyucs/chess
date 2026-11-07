package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GameDAO {

    int addGame(@NotNull String gameName, @NotNull ChessGame chessGame) throws AlreadyTakenException;

    GameData getGameByName(String gameName);

    GameData getGameByID(int gameID);

    void updatePlayerNames(int gameID, String whitePlayerName, String blackPlayerName);

    List<GameData> getAllGames();

    void deleteGame(int gameID);

    void clearDatabase();

}

