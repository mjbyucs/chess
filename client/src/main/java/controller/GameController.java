package controller;

import client.ResponseException;
import client.SessionService;
import model.GameList;
import ui.ClientView;

import java.util.Arrays;
import java.util.Set;

public class GameController {
    private static final Set<String> ALWAYS_VALID_COMMANDS = Set.of("help", "quit");
    private static final Set<String> SIGNED_OUT_COMMANDS = Set.of("register", "login");
    private static final Set<String> SIGNED_IN_COMMANDS = Set.of("logout", "create", "list", "observe", "join");

    private final ClientView clientView;
    private final SessionService sessionService;

    public GameController(ClientView clientView, SessionService sessionService) {
        this.clientView = clientView;
        this.sessionService = sessionService;
    }

    public void runGame() {
        boolean isSessionActive = true;
        clientView.displayWelcomeMessage();
        while (isSessionActive) {
            showPrompt();
            String input = clientView.getUserRequest();

            String[] command = parseInput(input);
            if (command.length > 0 && isValidCommand(command[0])) {
                switch(command[0]) {
                    case "help" -> showHelp();
                    case "quit" -> {
                        executeLogoutCmd(new String[]{"logout"});
                        clientView.displayMessage("Thanks for playing!\n");
                        isSessionActive = false;
                    }
                    case "register" -> executeRegisterCmd(command);
                    case "login" -> executeLoginCmd(command);
                    case "logout" -> executeLogoutCmd(command);
                    case "create" -> executeCreateGameCmd(command);
                    case "list" -> executeListGameCmd(command);
                    case "observe" -> {
                        executeObserveGameCmd(command);
                        clientView.displayChessBoard(sessionService.getGameData(), sessionService.getPerspective(),
                                clientView.mapInternalGameIdToViewId(sessionService.getLocalGameNumber()));
                    }
                    case "join" -> {
                        executeJoinGameCmd(command);
                        clientView.displayChessBoard(sessionService.getGameData(), sessionService.getPerspective(),
                                clientView.mapInternalGameIdToViewId(sessionService.getLocalGameNumber()));
                    }
                }
            }
            else {
                clientView.displayErrorMessage("You didn't enter a valid command. The valid commands are:");
                showHelp();
            }
        }
    }

    private String[] parseInput(String input) {
        String trimmed = input.strip();
        return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
    }

    private boolean isValidCommand(String cmd) {
        Set<String> validCommands = sessionService.isUserLoggedIn() ? SIGNED_IN_COMMANDS : SIGNED_OUT_COMMANDS;
        return ALWAYS_VALID_COMMANDS.contains(cmd) || validCommands.contains(cmd);
    }

    private void showPrompt() {
        if (sessionService.isUserLoggedIn()) {
            clientView.displayPostLoginPrompt();
        }
        else {
            clientView.displayPreLoginPrompt();
        }
    }

    private void showHelp() {
        if (sessionService.isUserLoggedIn()) {
            clientView.displayPostLoginHelp();
        }
        else {
            clientView.displayPreLoginHelp();
        }
    }

    private void executeRegisterCmd(String[] command) {
        if (command.length != 4) {
            clientView.displayErrorMessage("Invalid parameters for 'register'. Supply USERNAME, PASSWORD, and EMAIL.");
        }
        else {
            try {
                sessionService.registerUser(command[1], command[2], command[3]);
            }
            catch (ResponseException ex) {
                displayCommandError("Registration failed.", ex);
            }
        }
    }

    private void executeLoginCmd(String[] command) {
        if (command.length != 3) {
            clientView.displayErrorMessage("Invalid parameters for 'login'. Supply USERNAME, PASSWORD.");
        }
        else {
            try {
                sessionService.loginUser(command[1], command[2]);
            }
            catch (ResponseException ex) {
                displayCommandError("Login failed.", ex);
            }
        }
    }

    private void executeLogoutCmd(String[] command) {
        if (command.length != 1) {
            clientView.displayErrorMessage("'logout' does not take additional parameters. Did you intend to logout?");
        }
        else {
            try {
                sessionService.logoutUser();
            }
            catch (ResponseException ex) {
                displayCommandError("Logout failed.", ex);
            }
        }
    }

    private void executeCreateGameCmd(String[] command) {
        if (command.length < 2) {
            clientView.displayErrorMessage("Invalid parameters for 'create'. Supply game NAME.");
        }
        else {
            try {
                String gameName = String.join(" ", Arrays.copyOfRange(command, 1, command.length));
                int gameId = sessionService.createGame(gameName);
                clientView.displayMessage("Game '" + gameName + "' successfully created. It is game number " +
                                            clientView.mapInternalGameIdToViewId(gameId) + " if you'd like to join it.");
            }
            catch (ResponseException ex) {
                displayCommandError("Create game failed.", ex);
            }
        }
    }

    private void executeListGameCmd(String[] command) {
        if (command.length != 1) {
            clientView.displayErrorMessage("'list' does not take additional parameters. Did you intend to list games?");
        }
        else {
            try {
                GameList games = sessionService.listGames();
                clientView.displayGameList(games);
            }
            catch (ResponseException ex) {
                displayCommandError("List games failed.", ex);
            }
        }
    }

    private void executeObserveGameCmd(String[] command) {
        if (command.length != 2) {
            clientView.displayErrorMessage("Invalid parameters for 'observe'. Supply game ID.");
        }
        else {
            try {
                int gameId = clientView.mapViewGameIdToInternalId(Integer.parseInt(command[1]));
                sessionService.observeGame(gameId);
                clientView.displayMessage("You are successfully watching game " + gameId + ".");
            }
            catch (NumberFormatException nfex) {
                clientView.displayErrorMessage("Game ID must be a valid integer.");
            }
            catch (ResponseException ex) {
                displayCommandError("Observe game failed.", ex);
            }
        }
    }

    private void executeJoinGameCmd(String[] command) {
        if (command.length != 3 || !Set.of("WHITE", "BLACK").contains(command[2].toUpperCase())) {
            clientView.displayErrorMessage("Invalid parameters for 'join'. Supply game ID and COLOR [WHITE|BLACK].");
        }
        else {
            try {
                int gameId = clientView.mapViewGameIdToInternalId(Integer.parseInt(command[1]));
                String playerColor = command[2].toUpperCase();
                sessionService.joinGame(gameId, playerColor);
                clientView.displayMessage("Game successfully joined. You are the " + playerColor + " player");
            }
            catch (NumberFormatException nfex) {
                clientView.displayErrorMessage("Game ID must be a valid integer.");
            }
            catch (ResponseException ex) {
                displayCommandError("Join game failed.", ex);
            }
        }
    }

    private void displayCommandError(String message, ResponseException ex) {
        clientView.displayErrorMessage(String.format("%s %s", message,
                ex.getMessage() == null ? "" : ex.getMessage()));

    }
}
