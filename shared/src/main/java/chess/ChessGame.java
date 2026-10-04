package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private static final int CASTLE_COLS = 2;
    private ChessBoard gameBoard;
    private SpecialMovesState gameMoveState;
    private TeamColor whoseMove;

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        gameMoveState = new SpecialMovesState(gameBoard);
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

    public static TeamColor opponentColor(TeamColor color) {
        return color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
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
        List<ChessMove> possibleMoves = new ArrayList<>(piece.pieceMoves(board, startPosition));
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
        if (moveState.isCastleLeftLegal(startPosition, piece) &&
                allCastleConditionsSatisfied(board, piece, startPosition, 1)) {
            castleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                                                                       startPosition.getColumn() - CASTLE_COLS)));
        }
        if (moveState.isCastleRightLegal(startPosition, piece) &&
                allCastleConditionsSatisfied(board, piece, startPosition, ChessBoard.getBoardSize())) {
            castleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                    startPosition.getColumn() + CASTLE_COLS)));
        }
        return castleMoves;
    }

    private boolean allCastleConditionsSatisfied(ChessBoard board, ChessPiece piece, ChessPosition startPosition, int rookCol) {
        int kingCol = startPosition.getColumn();
        ChessPiece rookPiece = new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK);
        if (!rookPiece.equals(board.getPiece(new ChessPosition(startPosition.getRow(), rookCol)))) {
            return false;
        }
        // queen-side castle
        if (rookCol < kingCol) {
            return positionsEmpty(board, startPosition.getRow(), rookCol + 1, kingCol - 1) &&
                    positionsSafe(board, piece.getTeamColor(), startPosition.getRow(), kingCol - CASTLE_COLS, kingCol);
        }
        // king-side castle
        else {
            return positionsEmpty(board, startPosition.getRow(), kingCol + 1, rookCol - 1) &&
                    positionsSafe(board, piece.getTeamColor(), startPosition.getRow(), kingCol, kingCol + CASTLE_COLS);
        }
    }

    private boolean positionsEmpty(ChessBoard board, int row, int startCol, int endCol) {
        return IntStream.range(startCol, endCol + 1).allMatch(col -> board.isEmpty(new ChessPosition(row, col)));
    }

    private boolean positionsSafe(ChessBoard board, TeamColor color, int row, int startCol, int endCol) {
        return IntStream.range(startCol, endCol + 1).noneMatch(col -> isPositionThreatened(board, color, new ChessPosition(row, col)));
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
        setTeamTurn(opponentColor(getTeamTurn()));
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
        ChessPiece pieceToMove = move.getPromotionPiece() == null ? piece :
                                    new ChessPiece(colorToMove, move.getPromotionPiece());

        // check to see if this was an en passant move. If so, we need to remove the appropriate pawn too
        ChessPosition enPassantPos = moveState.getEnPassantPosition(piece, move.getStartPosition());
        if (move.getEndPosition().equals(enPassantPos)) {
            ChessPosition positionOfPawnToRemove = new ChessPosition(move.getStartPosition().getRow(),
                                                                     move.getEndPosition().getColumn());
            movePiece(board, null, positionOfPawnToRemove, positionOfPawnToRemove);
        }

        // check to see if this was a castle move. If so, we need to move the appropriate rook too
        if (getCastleMoves(board, moveState, pieceToMove, move.getStartPosition()).contains(move)) {
            boolean castledLeft = move.getEndPosition().getColumn() < move.getStartPosition().getColumn();
            int rookFinalCol = castledLeft ? move.getEndPosition().getColumn() + 1 : move.getEndPosition().getColumn() - 1;
            ChessPosition rookStartPos = new ChessPosition(move.getStartPosition().getRow(), castledLeft ? 1 : ChessBoard.getBoardSize());
            ChessPosition rookEndPos = new ChessPosition(move.getStartPosition().getRow(), rookFinalCol);
            movePiece(board, board.getPiece(rookStartPos), rookStartPos, rookEndPos);
        }

        // finally make the original move
        movePiece(board, pieceToMove, move.getStartPosition(), move.getEndPosition());
    }

    /**
     * Use this to "move" a piece on the board. To simply "remove" a piece on the board,
     * pass a null piece and the same start and end positions
     * @param board the board to operate on
     * @param piece the piece to move; can be null to simply remove the piece at a location
     * @param startPos position the piece starts at
     * @param endPos position to move it to
     */
    private void movePiece(ChessBoard board, ChessPiece piece, ChessPosition startPos, ChessPosition endPos) {
        board.addPiece(endPos, piece);
        board.addPiece(startPos, null);
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

    // find the king and see if it is threatened. If no king, there is no threat.
    private boolean isInCheckOnBoard(ChessBoard board, TeamColor teamColor) {
        Collection<ChessBoard.PiecePosition> piecePositions = board.getPiecePositions();

        // first find target king on the board. if we can't, we are in trouble.
        for (ChessBoard.PiecePosition piecePosition :piecePositions) {
            if (piecePosition.piece().getTeamColor() == teamColor &&
                    piecePosition.piece().getPieceType() == ChessPiece.PieceType.KING) {
                return isPositionThreatened(board, teamColor, piecePosition.position());
            }
        }
        return false;
    }

    private static boolean isPositionThreatened(ChessBoard board, TeamColor teamColor, ChessPosition position) {
        Objects.requireNonNull(position);
        Collection<ChessBoard.PiecePosition> piecePositions = board.getPiecePositions();

        // run through all opponent pieces, generate their moves and see if one includes target king's position
        for (ChessBoard.PiecePosition piecePosition : piecePositions) {
            if (piecePosition.piece().getTeamColor() != teamColor) {
                for (ChessMove pieceMove : board.getPiece(piecePosition.position()).pieceAttacks(board, piecePosition.position())) {
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
     * Sets this game's chessboard to a copy of the given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        Objects.requireNonNull(board);
        gameBoard = new ChessBoard(board);
        gameMoveState = new SpecialMovesState(gameBoard);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        // return a copy so that the user can't modify our copy
        return new ChessBoard(gameBoard);
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
