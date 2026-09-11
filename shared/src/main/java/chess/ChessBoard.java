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
    private static final int BOARD_DIMENSION = 10;
    private final ChessPiece[] board;

    public ChessBoard() {
        board = createEmptyBoard();
    }

    private ChessPiece[] createEmptyBoard() {
        return new ChessPiece[BOARD_DIMENSION * BOARD_DIMENSION];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (!isInbounds(position)) {
            throw new RuntimeException(String.format("Row [%d], column [%d] is not a valid board position",
                                                     position.getRow(), position.getColumn()));
        }
        board[positionToIndex(position)] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[positionToIndex(position)];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        int row = 8;
        int column = 0;
        for (var c : defaultBoard.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 0;
                    row--;
                }
                case '|' -> column++;
                default -> {
                    ChessPiece piece = null;
                    if (!Character.isSpaceChar(c)) {
                        ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                                : ChessGame.TeamColor.WHITE;
                        var type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));
                        piece = new ChessPiece(color, type);
                    }
                    board[rowColToIndex(row, column)] = piece;
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
        return Arrays.hashCode(board);
    }

    public static int rowColToIndex(ChessPosition position) {
        return rowColToIndex(position.getRow(), position.getColumn());
    }

    public static int rowColToIndex(int row, int col) {
        return row * BOARD_DIMENSION + col;
    }

    public static int positionToIndex(ChessPosition position) {
        return position.getRow() * BOARD_DIMENSION + position.getColumn();
    }

    public static boolean isIndexOnboard(int idx) {
        int row = idx / BOARD_DIMENSION;
        int col = idx % BOARD_DIMENSION;
        return 1 <= row && row <= 8 && 1 <= col && col <= 8;
    }

    // make this package accessible so move generators can use it to check for valid positions
    protected static boolean isInbounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return 1 <= row && row <= 8 && 1 <= col && col <= 8;
    }

    // "borrowed" from the TestUtilities class
    private static final String defaultBoard = """
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

}
