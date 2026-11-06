package client;

import controller.GameController;
import model.SessionState;
import ui.ClientView;
import ui.ConsoleBoardRenderer;
import ui.ConsoleClientView;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("♕ CS240 Chess Client");

        int defaultPort = 8080;
        if (args.length == 3 && "--port".equals(args[1])) {
            defaultPort = Integer.parseInt(args[2]);
        }
        SessionService sessionService = new SessionService(new SessionState(), new ServerFacade(defaultPort));
        ClientView clientView = new ConsoleClientView(new ConsoleBoardRenderer());
        GameController gameController = new GameController(clientView, sessionService);
        gameController.runGame();
    }
}
