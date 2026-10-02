package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private static final int CASTLE_COLS = 2;
    private ChessBoard gameBoard;
    private final SpecialMovesState gameMoveState;
    private TeamColor whoseMove;

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        gameMoveState = new SpecialMovesState();
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
        return gameBoard.isEmpty(startPosition) ? null : validMovesOnBoard(gameBoard, gameMoveState, startPosition);
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
    private Collection<ChessMove> validMovesOnBoard(ChessBoard board, SpecialMovesState moveState,
                                                    ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Objects.requireNonNull(piece, "piece");
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        ChessPosition enPassantPos = moveState.getEnPassantPosition(piece, startPosition);
        if (enPassantPos != null) {
            possibleMoves.add(new ChessMove(startPosition, enPassantPos));
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            possibleMoves.addAll(getCastleMoves(board, moveState, piece, startPosition));
        }
        return possibleMoves.stream()
                .filter(move -> !moveLeavesTeamInCheck(board, moveState, move, piece.getTeamColor()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Collection<ChessMove> getCastleMoves(ChessBoard board, SpecialMovesState moveState,
                                                 ChessPiece piece, ChessPosition startPosition) {
        List<ChessMove> castleMoves = new ArrayList<>();
        if (piece.getPieceType() == ChessPiece.PieceType.KING && moveState.isCastleLeftLegal(startPosition, piece) &&
                positionsEmpty(board, startPosition.getRow(), 2, startPosition.getColumn() - 1) &&
                positionsSafe(board, piece.getTeamColor(), startPosition.getRow(),
                        startPosition.getColumn() - CASTLE_COLS, startPosition.getColumn())) {
            castleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                                                                       startPosition.getColumn() - CASTLE_COLS)));
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING && moveState.isCastleRightLegal(startPosition, piece) &&
                positionsEmpty(board, startPosition.getRow(), startPosition.getColumn() + 1, ChessBoard.getBoardSize() - 1) &&
                positionsSafe(board, piece.getTeamColor(), startPosition.getRow(), startPosition.getColumn(),
                        startPosition.getColumn() + CASTLE_COLS)) {
            castleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() + CASTLE_COLS)));
        }
        return castleMoves;
    }

    private boolean positionsEmpty(ChessBoard board, int row, int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++) {
            if (!board.isEmpty(new ChessPosition(row, col))) {
                return false;
            }
        }
        return true;
    }

    private boolean positionsSafe(ChessBoard board, TeamColor color, int row, int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++) {
            if (isPositionThreatened(board, color, new ChessPosition(row, col))) {
                return false;
            }
        }
        return true;
    }

    private boolean moveLeavesTeamInCheck(ChessBoard board, SpecialMovesState moveState,
                                          ChessMove move, TeamColor teamColor) {
        ChessBoard tmpBoard = new ChessBoard(board);
        SpecialMovesState tmpMoveState = new SpecialMovesState(moveState);
        executeMoveOnBoard(tmpBoard, tmpMoveState, move, teamColor);
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
        makeMoveOnBoard(gameBoard, gameMoveState, move, getTeamTurn());
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Make moves on any given board for any given color
     * @param board the board to operate on
     * @param move the move to make
     * @param colorToMove the color making the move
     * @throws InvalidMoveException if the move is not valid
     */
    private void makeMoveOnBoard(ChessBoard board, SpecialMovesState moveState,
                                 ChessMove move, TeamColor colorToMove) throws InvalidMoveException {
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
        if (!validMovesOnBoard(board, moveState, startPos).contains(move)) {
            throw new InvalidMoveException("This move isn't legal with the current board state");
        }

        executeMoveOnBoard(board, moveState, move, colorToMove);
        moveState.recordMove(piece, move);
    }

    private void executeMoveOnBoard(ChessBoard board, SpecialMovesState moveState,
                                    ChessMove move, TeamColor colorToMove) {
        // trust the caller and make the move regardless
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece movePiece = move.getPromotionPiece() == null ? piece :
                                                            new ChessPiece(colorToMove, move.getPromotionPiece());

        // check to see if this was an en passant move. If so, we need to remove the appropriate pawn too
        ChessPosition enPassantPos = moveState.getEnPassantPosition(piece, move.getStartPosition());
        if (move.getEndPosition().equals(enPassantPos)) {
            board.addPiece(new ChessPosition(move.getStartPosition().getRow(),
                                             move.getEndPosition().getColumn()),
                            null);
        }

        // check to see if this was a castle move. If so, we need to move the appropriate rook too
        if (getCastleMoves(board, moveState, movePiece, move.getStartPosition()).contains(move)) {
            boolean castledLeft = move.getEndPosition().getColumn() < move.getStartPosition().getColumn();
            int rookFinalCol = castledLeft ? move.getEndPosition().getColumn() + 1 : move.getEndPosition().getColumn() - 1;
            ChessMove rookMove = new ChessMove(new ChessPosition(move.getStartPosition().getRow(),
                                                                 castledLeft ? 1 : 8),
                                               new ChessPosition(move.getStartPosition().getRow(), rookFinalCol));
            executeMoveOnBoard(board, moveState, rookMove, colorToMove);
        }

        board.addPiece(move.getEndPosition(), movePiece);
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
        for (ChessBoard.PiecePosition piecePosition :piecePositions) {
            if (piecePosition.piece().getTeamColor() == teamColor &&
                    piecePosition.piece().getPieceType() == ChessPiece.PieceType.KING) {
                return isPositionThreatened(board, teamColor, piecePosition.position());
            }
        }
        return false;
    }

    private static boolean isPositionThreatened(ChessBoard board, TeamColor teamColor, ChessPosition position) {
        // if we get here without finding the target king, we are in trouble
        Objects.requireNonNull(position);
        Collection<ChessBoard.PiecePosition> piecePositions = board.getPiecePositions();

        // run through all opponent pieces, generate their moves and see if one includes target king's position
        for (ChessBoard.PiecePosition piecePosition : piecePositions) {
            if (piecePosition.piece().getTeamColor() != teamColor) {
                for (ChessMove pieceMove : board.getPiece(piecePosition.position()).pieceMoves(board, piecePosition.position())) {
                    if (pieceMove.getEndPosition().equals(position)) {
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
                allMoves.addAll(validMovesOnBoard(gameBoard, gameMoveState, piecePosition.position()));
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
        return Objects.equals(gameBoard, chessGame.gameBoard) && Objects.equals(gameMoveState, chessGame.gameMoveState) && whoseMove == chessGame.whoseMove;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, gameMoveState, whoseMove);
    }
}
