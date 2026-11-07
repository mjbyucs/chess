package ui;

import chess.ChessGame;
import model.GameData;
import model.GameList;
import model.GameListEntry;

import java.util.Scanner;

public class ConsoleClientView implements ClientView {
    private static final String QUIT_HELP_COMMANDS = formatCommand("quit", "playing chess") +
                                                     formatCommand("help", "with possible commands");
    private final Scanner scanner = new Scanner(System.in);
    private final BoardRenderer boardRenderer;

    public ConsoleClientView(BoardRenderer boardRenderer) {
        this.boardRenderer = boardRenderer;
    }

    @Override
    public void displayWelcomeMessage() {
        System.out.println("Welcome to Chess. Type 'help' to get started.");
    }

    @Override
    public void displayPreLoginPrompt() {
        System.out.print("[LOGGED OUT] >>> ");
    }

    @Override
    public void displayPostLoginPrompt() {
        System.out.print("[LOGGED IN] >>> ");
    }

    @Override
    public String getUserRequest() {
        return scanner.nextLine();
    }

    @Override
    public void displayPreLoginHelp() {
        System.out.println(
                formatCommand("register <USERNAME> <PASSWORD> <EMAIL>", "to create an account") +
                formatCommand("login <USERNAME> <PASSWORD>", "to play chess") +
                QUIT_HELP_COMMANDS
        );
    }

    @Override
    public void displayPostLoginHelp() {
        System.out.println(
                formatCommand("create <NAME>", "a game") +
                formatCommand("list", "games") +
                formatCommand("join <ID> [WHITE|BLACK]", "a game") +
                formatCommand("observe <ID>", "a game") +
                formatCommand("logout", "when you are done") +
                QUIT_HELP_COMMANDS
        );

    }

    private static String formatCommand(String cmd, String description) {
        return String.format("  %s%s%s - %s\n", EscapeSequences.SET_TEXT_COLOR_GREEN, cmd,
                                                EscapeSequences.RESET_TEXT_COLOR, description);
    }

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void displayErrorMessage(String message) {
        System.out.printf("%s%s%s\n", EscapeSequences.SET_TEXT_COLOR_RED, message, EscapeSequences.RESET_TEXT_COLOR);
    }

    @Override
    public void displayChessBoard(GameData gameData, ChessGame.TeamColor perspective, int gameId) {
        if (gameData != null && perspective != null) {
            boardRenderer.renderChessBoard(gameData, perspective, gameId);
        }
    }

    @Override
    public void displayGameList(GameList games) {
        if (games.isEmpty()) {
            System.out.println("No games have been created yet.");
        }
        else {
            for (int i = 0; i < games.size(); i++) {
                GameListEntry gameItem = games.get(i);
                System.out.printf("  %2d. %s\t\tWhite: %s\tBlack: %s\n",
                        mapInternalGameIdToViewId(i),
                        gameItem.gameName(),
                        playerName(gameItem.whiteUsername()),
                        playerName(gameItem.blackUsername()));
            }
        }
    }

    @Override
    public int mapInternalGameIdToViewId(int gameId) {
        return gameId + 1;
    }

    @Override
    public int mapViewGameIdToInternalId(int gameId) {
        return gameId - 1;
    }

    private String playerName(String playerName) {
        return playerName == null ? "-" : playerName;
    }
}
