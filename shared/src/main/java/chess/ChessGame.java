package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard gameBoard;
    private TeamColor whoseMove;

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        whoseMove = TeamColor.WHITE;        // white starts
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return whoseMove;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        Objects.requireNonNull(team, "team color");
        whoseMove = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Objects.requireNonNull(gameBoard, "game board");
        return gameBoard.isEmpty(startPosition) ? null :
                gameBoard.getPiece(startPosition).pieceMoves(gameBoard, startPosition);
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Objects.requireNonNull(move, "move");
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();

        if (gameBoard.isEmpty(startPos)) {
            throw new InvalidMoveException("No piece at position [" + startPos.getRow() + ", " + startPos.getColumn() + "]");
        }
        ChessPiece piece = gameBoard.getPiece(startPos);

        // ensure this move is for the current team
        if (whoseMove != piece.getTeamColor()) {
            throw new InvalidMoveException("Move is for a " + piece.getTeamColor() + " piece but it is " + whoseMove + "'s turn.");
        }
        if (!validMoves(startPos).contains(move)) {
            throw new InvalidMoveException("This move isn't legal with the current board state");
        }

        // make the move
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        gameBoard.addPiece(endPos, piece);
        gameBoard.addPiece(startPos, null);
        whoseMove = whoseMove == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
