package controller;

import client.ServerFacade;
import client.SessionService;
import model.SessionState;
import org.junit.jupiter.api.Assertions;
import ui.ConsoleBoardRenderer;
import ui.ConsoleClientView;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Scanner;

public class GameControllerTests {
    @Test
    void eofExitsClientCleanly() {
        var view = new ConsoleClientView(
                new Scanner(new StringReader("")),
                new ConsoleBoardRenderer()
        );

        var sessionService = new SessionService(
                new SessionState(),
                new ServerFacade(8080)
        );

        var controller = new GameController(view, sessionService);

        Assertions.assertDoesNotThrow(controller::runGame);
    }
}
