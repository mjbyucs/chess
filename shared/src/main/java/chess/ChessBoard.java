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
    private static final int PLAYABLE_BOARD_SIZE = 8;
    private static final int BOARD_STORAGE_SIZE = PLAYABLE_BOARD_SIZE + 1;
    // waste a little space to keep the board coordinates easy to manage (mostly for debugging)
    private final ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[BOARD_STORAGE_SIZE][BOARD_STORAGE_SIZE];
    }


    /*
     * Pack level method to get the size of the board
     */
    static int getBoardSize() {
        return PLAYABLE_BOARD_SIZE;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        validatePosition(position);
        board[position.getRow()][position.getColumn()] = piece;
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
        return board[position.getRow()][position.getColumn()];
    }

    boolean isEmpty(ChessPosition position) {
        validatePosition(position);
        return board[position.getRow()][position.getColumn()] == null;
    }

    boolean canMoveTo(ChessPosition position, ChessGame.TeamColor myColor) {
        Objects.requireNonNull(myColor, "color");
        ChessPiece piece = getPiece(position);
        return piece == null || piece.getTeamColor() != myColor;
    }

    boolean isOpponentPiece(ChessPosition position, ChessGame.TeamColor myColor) {
        Objects.requireNonNull(myColor, "color");
        ChessPiece piece = getPiece(position);
        return piece != null && piece.getTeamColor() != myColor;
    }

    boolean isStartingRow(ChessPosition position, ChessPiece piece) {
        ChessGame.TeamColor color = piece.getTeamColor();
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            return color == ChessGame.TeamColor.WHITE ? position.getRow() == 2 : position.getRow() == PLAYABLE_BOARD_SIZE - 1;
        }
        else {
            return color == ChessGame.TeamColor.WHITE ? position.getRow() == 1 : position.getRow() == PLAYABLE_BOARD_SIZE;
        }
    }

    boolean isPromotionRank(ChessPosition position, ChessGame.TeamColor pawnColor) {
        return pawnColor == ChessGame.TeamColor.WHITE ?
                position.getRow() == PLAYABLE_BOARD_SIZE :
                position.getRow() == 1;
    }

    private void validatePosition(ChessPosition position) {
        Objects.requireNonNull(position, "position");
        if (!position.isValid()) {
            throw new IllegalArgumentException("Invalid board position {" + position.getRow() + ", " + position.getColumn() + "}");
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
        int row = PLAYABLE_BOARD_SIZE;
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
