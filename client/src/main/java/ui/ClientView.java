package ui;

import chess.ChessGame;
import model.GameData;

public interface ClientView {
    void displayWelcomeMessage();
    void displayPreLoginPrompt();
    void displayPostLoginPrompt();
    String getUserRequest();
    void displayPreLoginHelp();
    void displayPostLoginHelp();
    void displayMessage(String message);
    void displayErrorMessage(String message);
    void displayChessBoard(GameData gameData, ChessGame.TeamColor perspective);
}
