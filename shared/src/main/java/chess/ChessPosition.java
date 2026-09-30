package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = Objects.requireNonNull(row, "row");
        this.col = Objects.requireNonNull(col, "col");
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left column
     */
    public int getColumn() {
        return col;
    }

    public ChessPosition add(ChessPosition pos) {
        return new ChessPosition(this.row + pos.row, this.col + pos.col);
    }

    /*
     * Calculate if the position resides on the board (ie within allowed coordinates)
     */
    public boolean isValid() {
        return row >= 1 && row <= ChessBoard.getBoardSize() && col >= 1 && col <= ChessBoard.getBoardSize();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
