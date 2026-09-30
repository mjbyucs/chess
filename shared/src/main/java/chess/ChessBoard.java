package chess;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    final static int BOARD_SIZE = 8;
    // waste a little space to keep the board coordinates easy to manage (mostly for debugging)
    private final ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[BOARD_SIZE+1][BOARD_SIZE+1];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        validatePosition(position);
        board[position.row][position.col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        validatePosition(position);
        return board[position.row][position.col];
    }

    public boolean isEmpty(ChessPosition position) {
        validatePosition(position);
        return board[position.row][position.col] == null;
    }

    public boolean canMoveTo(ChessPosition position, ChessGame.TeamColor myColor) {
        ChessPiece piece = getPiece(position);
        return piece == null || piece.getTeamColor() != myColor;
    }

    public boolean isOpponentPiece(ChessPosition position, ChessGame.TeamColor myColor) {
        ChessPiece piece = getPiece(position);
        return piece != null && piece.getTeamColor() != myColor;
    }

    public boolean isStartingRow(ChessPosition position, ChessPiece piece) {
        ChessGame.TeamColor color = piece.getTeamColor();
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            return color == ChessGame.TeamColor.WHITE ? position.row == 2 : position.row == BOARD_SIZE - 1;
        }
        else {
            return color == ChessGame.TeamColor.WHITE ? position.row == 1 : position.row == BOARD_SIZE;
        }
    }

    private void validatePosition(ChessPosition position) {
        if (!position.isValid()) {
            throw new RuntimeException("Invalid board position {" + position.row + ", " + position.col + "}");
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        loadBoard();
    }

    /*
     * Borrow some code from TestUtilities to load a board
     */
    private static final String defaultBoard =
            """
                |r|n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|
                """;

    private static final Map<Character, ChessPiece.PieceType> CHAR_TO_TYPE_MAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);

    private void loadBoard() {
        int row = BOARD_SIZE;
        int column = 0;
        for (var c : ChessBoard.defaultBoard.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 0;
                    row--;
                }
                case '|' -> column++;
                default -> {
                    ChessPiece piece = null;
                    ChessPosition position = new ChessPosition(row, column);
                    if (!Character.isSpaceChar(c)) {
                        ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                                : ChessGame.TeamColor.WHITE;
                        var type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));
                        piece = new ChessPiece(color, type);
                    }
                    addPiece(position, piece);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
