package ui;

import chess.ChessGame;
import model.GameData;

public interface BoardRenderer {
    void renderChessBoard(GameData chessGame, ChessGame.TeamColor perspective, int gameId);
}
