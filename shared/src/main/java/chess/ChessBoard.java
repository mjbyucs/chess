package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private static final int BOARD_DIMENSION = 10;
    private ChessPiece[] board;

    public ChessBoard() {
        board = new ChessPiece[BOARD_DIMENSION * BOARD_DIMENSION];
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
    }

    private int positionToIndex(ChessPosition position) {
        return position.getRow() * BOARD_DIMENSION + position.getColumn();
    }

    // make this package accessible so move generators can use it to check for valid positions
    protected static boolean isInbounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return 1 <= row && row <= 8 && 1 <= col && col <= 8;
    }

}
