package ui;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;

import java.util.stream.IntStream;

public class ConsoleBoardRenderer implements BoardRenderer {
    private static final int SQUARE_WIDTH = 3;
    private static final int SQUARE_HEIGHT = 1;
    private static final int RENDER_WIDTH = SQUARE_WIDTH * (2 + ChessBoard.getBoardSize());
    private static final String BORDER_COLOR = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String LIGHT_SQ_COLOR = EscapeSequences.SET_BG_COLOR + "187m";
    private static final String DARK_SQ_COLOR = EscapeSequences.SET_BG_COLOR + "65m";
    @Override
    public void renderChessBoard(GameData chessGame, ChessGame.TeamColor perspective) {
        System.out.println("Begin rendering board");
        renderEmptyBoard(chessGame.game());
        System.out.println(EscapeSequences.RESET_BG_COLOR +
                            EscapeSequences.RESET_TEXT_COLOR);
    }

    private void renderEmptyBoard(ChessGame chessGame) {
        System.out.print(EscapeSequences.ERASE_SCREEN + EscapeSequences.moveCursorToLocation(1, 1));
        System.out.print(horizontalBoardRow());
        for (int i = 0; i < ChessBoard.getBoardSize() / 2; i++) {
            System.out.print(chessBoardRow(LIGHT_SQ_COLOR, DARK_SQ_COLOR));
            System.out.print(chessBoardRow(DARK_SQ_COLOR, LIGHT_SQ_COLOR));
        }
        System.out.print(horizontalBoardRow());
    }

    private String horizontalBoardRow() {
        StringBuilder row = new StringBuilder();
        for (int h = 0; h < SQUARE_HEIGHT; h++) {
            row.append(BORDER_COLOR);
            IntStream.range(0, RENDER_WIDTH).forEach(i -> row.append(' '));
            row.append(EscapeSequences.RESET_BG_COLOR + "\n");
        }
        return row.toString();
    }

    private String chessBoardRow(String firstColColor, String secondColColor) {
        StringBuilder row = new StringBuilder();
        for (int h = 0; h < SQUARE_HEIGHT; h++) {
            row.append(BORDER_COLOR);
            IntStream.range(0, SQUARE_WIDTH).forEach(i -> row.append(' '));
            for (int sq = 0; sq < ChessBoard.getBoardSize() / 2; sq++) {
                row.append(firstColColor);
                IntStream.range(0, SQUARE_WIDTH).forEach(i -> row.append(' '));
                row.append(secondColColor);
                IntStream.range(0, SQUARE_WIDTH).forEach(i -> row.append(' '));
            }
            row.append(BORDER_COLOR);
            IntStream.range(0, SQUARE_WIDTH).forEach(i -> row.append(' '));
            row.append(EscapeSequences.RESET_BG_COLOR + "\n");
        }
        return row.toString();
    }

    public static void main(String[] args) {
        ConsoleBoardRenderer renderer = new ConsoleBoardRenderer();
        GameData game = new GameData(999, "Betty White", "Jack Black",
                                    "The GAME", new ChessGame());
        renderer.renderChessBoard(game, ChessGame.TeamColor.WHITE);
    }
}
