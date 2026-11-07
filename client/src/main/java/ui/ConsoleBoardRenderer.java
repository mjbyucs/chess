package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Scanner;

public class ConsoleBoardRenderer implements BoardRenderer {
    private static final String BORDER_BG_COLOR = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_FG_COLOR = EscapeSequences.SET_TEXT_COLOR + "22m";
    private static final String LIGHT_SQ_COLOR = EscapeSequences.SET_BG_COLOR + "187m";
    private static final String DARK_SQ_COLOR = EscapeSequences.SET_BG_COLOR + "65m";
    private static final String LABEL_COLOR = EscapeSequences.SET_TEXT_COLOR_RED;
    private static final String PLAYER_NAME_COLOR = EscapeSequences.SET_TEXT_COLOR_WHITE;

    @Override
    public void renderChessBoard(GameData chessGame, ChessGame.TeamColor perspective, int gameId) {
        System.out.print(produceCompleteRendering(chessGame, perspective, gameId));
    }

    // package access in case we want to test the string generated
    String produceCompleteRendering(GameData chessGame, ChessGame.TeamColor perspective, int gameId) {
        StringBuilder rendering = new StringBuilder();
        String whitePlayerName = formatPlayerName(ChessGame.TeamColor.WHITE, chessGame.whiteUsername());
        String blackPlayerName = formatPlayerName(ChessGame.TeamColor.BLACK, chessGame.blackUsername());

        char[] files;
        char[] ranks;
        if (perspective == ChessGame.TeamColor.WHITE) {
            files = "abcdefgh".toCharArray();
            ranks = "87654321".toCharArray();
        }
        else {
            files = "hgfedcba".toCharArray();
            ranks = "12345678".toCharArray();
        }
        // clear the board, move the cursor to the top, and produce the game ID
        rendering.append('\n')
                .append(EscapeSequences.ERASE_SCREEN)
                .append(EscapeSequences.moveCursorToLocation(1, 1))
                .append(EscapeSequences.SET_BG_COLOR_BLACK)
                .append(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY)
                .append("Game: ")
                .append(gameId)
                .append(EscapeSequences.RESET_BG_COLOR)
                .append(EscapeSequences.RESET_TEXT_COLOR)
                .append('\n');

        rendering.append(drawPlayerName(perspective == ChessGame.TeamColor.WHITE ? blackPlayerName : whitePlayerName));
        rendering.append(drawFiles(files));
        rendering.append(drawBoardWithRanks(chessGame.game().getBoard(), ranks));
        rendering.append(drawFiles(files));
        rendering.append(drawPlayerName(perspective == ChessGame.TeamColor.WHITE ? whitePlayerName : blackPlayerName));
        // clean the rest of the terminal and prepare it for user interaction
        rendering.append(EscapeSequences.RESET_BG_COLOR)
                .append(EscapeSequences.RESET_TEXT_COLOR)
                .append('\n');
        return rendering.toString();
    }

    private String formatPlayerName(ChessGame.TeamColor color, String username) {
        return String.format("%15s: %-13s", color.name() + " Player",username);
    }

    private String drawBoardWithRanks(ChessBoard chessBoard, char[] ranks) {
        StringBuilder board = new StringBuilder();
        int renderRow = 1;
        for (char rank : ranks) {
            drawRankColumn(board, rank);
            int r = Character.digit(rank, 10);
            for (int c = 1; c <= ChessBoard.getBoardSize(); c++) {
                String bgColor = ((renderRow + c) % 2) == 0 ? LIGHT_SQ_COLOR : DARK_SQ_COLOR;
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(r, c));
                String renderedPiece = mapPieceToRendered(piece);
                board.append(bgColor).append(renderedPiece);
            }
            drawRankColumn(board, rank);
            board.append(EscapeSequences.RESET_BG_COLOR).append(EscapeSequences.RESET_TEXT_COLOR).append('\n');
            renderRow++;
        }
        return board.toString();
    }

    private void drawRankColumn(StringBuilder builder, char rank) {
        builder.append(BORDER_BG_COLOR)
                .append(BORDER_FG_COLOR).append(' ')
                .append(LABEL_COLOR).append(rank)
                .append(BORDER_FG_COLOR).append(' ');
    }

    private String drawPlayerName(String playerName) {
        return String.format("%s%s%s%s%s\n", BORDER_BG_COLOR, PLAYER_NAME_COLOR, playerName,
                EscapeSequences.RESET_BG_COLOR, EscapeSequences.RESET_TEXT_COLOR);
    }

    private String drawFiles(char[] files) {
        StringBuilder filesRow = new StringBuilder();
        filesRow.append(BORDER_BG_COLOR).append(BORDER_FG_COLOR).append(EscapeSequences.EMPTY);
        for (char file : files) {
            filesRow.append(BORDER_FG_COLOR).append(' ')
                    .append(LABEL_COLOR).append(file)
                    .append(BORDER_FG_COLOR).append(' ');
        }
        filesRow.append(BORDER_BG_COLOR).append(BORDER_FG_COLOR).append(EscapeSequences.EMPTY)
                .append(EscapeSequences.RESET_BG_COLOR).append(EscapeSequences.RESET_TEXT_COLOR).append("\n");
        return filesRow.toString();
    }

    private String mapPieceToRendered(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        String fgColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK;
        return fgColor + switch (piece.getPieceType()) {
            case ChessPiece.PieceType.KING -> EscapeSequences.BLACK_KING;
            case ChessPiece.PieceType.QUEEN -> EscapeSequences.BLACK_QUEEN;
            case ChessPiece.PieceType.ROOK -> EscapeSequences.BLACK_ROOK;
            case ChessPiece.PieceType.BISHOP -> EscapeSequences.BLACK_BISHOP;
            case ChessPiece.PieceType.KNIGHT -> EscapeSequences.BLACK_KNIGHT;
            case ChessPiece.PieceType.PAWN -> EscapeSequences.BLACK_PAWN;
        };
    }

    public static void main(String[] args) {
        ConsoleBoardRenderer renderer = new ConsoleBoardRenderer();
        GameData game = new GameData(999, "Betty White", "Jack Black",
                                    "The GAME", new ChessGame());
        renderer.renderChessBoard(game, ChessGame.TeamColor.WHITE, 999);

        System.out.print("Press enter to see the board from Black's perspective: ");
        new Scanner(System.in).nextLine();
        renderer.renderChessBoard(game, ChessGame.TeamColor.BLACK, 1000);
    }
}
