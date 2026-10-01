package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        return gameBoard.isEmpty(startPosition) ? null : validMovesOnBoard(gameBoard, startPosition);
    }

    /**
     * Generate all valid moves for a piece located at the given position on the
     * given board. This method is responsible for filtering out moves that violate
     * the rules of chess.
     * Warning: implementation assumes there is piece at the given position
     * @param board a ChessBoard to evaluate
     * @param startPosition the position from which to generate moves
     * @return collection of all legal chess moves
     */
    private Collection<ChessMove> validMovesOnBoard(ChessBoard board, ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Objects.requireNonNull(piece, "piece");
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        return possibleMoves.stream()
                .filter(move -> !moveLeavesTeamInCheck(board, move, piece.getTeamColor())).toList();
    }

    private boolean moveLeavesTeamInCheck(ChessBoard board, ChessMove move, TeamColor teamColor) {
        ChessBoard tmpBoard = new ChessBoard(board);
        executeMoveOnBoard(tmpBoard, move, teamColor);
        return isInCheckOnBoard(tmpBoard, teamColor);
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Objects.requireNonNull(move, "move");
        makeMoveOnBoard(gameBoard, move, getTeamTurn());
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Make moves on any given board for any given color
     * @param board the board to operate on
     * @param move the move to make
     * @param colorToMove the color making the move
     * @throws InvalidMoveException if the move is not valid
     */
    private void makeMoveOnBoard(ChessBoard board, ChessMove move, TeamColor colorToMove) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();

        if (board.isEmpty(startPos)) {
            throw new InvalidMoveException("No piece at position [" + startPos.getRow() + ", " + startPos.getColumn() + "]");
        }
        ChessPiece piece = board.getPiece(startPos);

        // ensure this move is for the current team
        if (colorToMove != piece.getTeamColor()) {
            throw new InvalidMoveException("Move is for a " + piece.getTeamColor() + " piece but it is " +
                                            colorToMove + "'s turn.");
        }
        if (!validMoves(startPos).contains(move)) {
            throw new InvalidMoveException("This move isn't legal with the current board state");
        }

        executeMoveOnBoard(board, move, colorToMove);
    }

    private static void executeMoveOnBoard(ChessBoard board, ChessMove move, TeamColor colorToMove) {
        // trust the caller and make the move regardless
        ChessPiece piece = move.getPromotionPiece() == null ? board.getPiece(move.getStartPosition()) :
                                                            new ChessPiece(colorToMove, move.getPromotionPiece());
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
    }


    /**
         * Determines if the given team is in check
         *
         * @param teamColor which team to check for check
         * @return True if the specified team is in check
         */
    public boolean isInCheck(TeamColor teamColor) {
        Objects.requireNonNull(teamColor, "teamColor");
        return isInCheckOnBoard(gameBoard, teamColor);
    }

    public boolean isInCheckOnBoard(ChessBoard board, TeamColor teamColor) {
        Collection<ChessBoard.PiecePosition> piecePositions = board.getPiecePositions();

        // first find target king on the board
        ChessBoard.PiecePosition myKingPosition = null;
        for (ChessBoard.PiecePosition piecePosition :piecePositions) {
            if (piecePosition.piece().getTeamColor() == teamColor &&
                    piecePosition.piece().getPieceType() == ChessPiece.PieceType.KING) {
                myKingPosition = piecePosition;
                break;
            }
        }
        // if we get here without finding the target king, we are in trouble
        Objects.requireNonNull(myKingPosition);

        // run through all opponent pieces, generate their moves and see if one includes target king's position
        for (ChessBoard.PiecePosition piecePosition : piecePositions) {
            if (piecePosition.piece().getTeamColor() != teamColor) {
                for (ChessMove pieceMove : board.getPiece(piecePosition.position()).pieceMoves(board, piecePosition.position())) {
                    if (pieceMove.getEndPosition().equals(myKingPosition.position())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Collection<ChessMove> getAllLegalMovesForColor(TeamColor teamColor) {
        List<ChessMove> allMoves = new ArrayList<>();
        for (ChessBoard.PiecePosition piecePosition : gameBoard.getPiecePositions()) {
            if (piecePosition.piece().getTeamColor() == teamColor) {
                allMoves.addAll(validMovesOnBoard(gameBoard, piecePosition.position()));
            }
        }
        return allMoves;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Objects.requireNonNull(teamColor, "teamColor");
        return isInCheck(teamColor) && getAllLegalMovesForColor(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Objects.requireNonNull(teamColor, "teamColor");
        return !isInCheck(teamColor) && getAllLegalMovesForColor(teamColor).isEmpty();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && whoseMove == chessGame.whoseMove;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, whoseMove);
    }
}
