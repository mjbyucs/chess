package chess;

import java.util.*;

/**
 * Class that keeps state about movement of pawns, rooks, and kings that can report if
 * en passant or castling are legal moves. The assumption is that this state is paired
 * with a particular board so if a board is copied, so should its state be copied.
 */
public class SpecialMovesState {
    private static final int FIRST_ROW_COL = 1;
    private static final int LAST_ROW_COL = 8;      // ChessBoard.getBoardSize();
    private static final int KING_STARTING_COL = 5;
    private ChessMove lastMoveIfPawn;
    private final Map<ChessGame.TeamColor, Set<CastlePieces>> castleEligible;

    private enum CastlePieces {
        KING, ROOK_1, ROOK_8
    }

    SpecialMovesState(ChessBoard board) {
        lastMoveIfPawn = null;
        // because we can get a board starting at any state, we have to check which
        // castle conditions are valid given the original board
        castleEligible = new EnumMap<>(ChessGame.TeamColor.class);
        castleEligible.put(ChessGame.TeamColor.WHITE, EnumSet.noneOf(CastlePieces.class));
        castleEligible.put(ChessGame.TeamColor.BLACK, EnumSet.noneOf(CastlePieces.class));
        addEligibility(board, ChessGame.TeamColor.WHITE, FIRST_ROW_COL);
        addEligibility(board, ChessGame.TeamColor.BLACK, LAST_ROW_COL);
    }

    private void addEligibility(ChessBoard board, ChessGame.TeamColor color, int row) {
        ChessPiece rook = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        ChessPiece king = new ChessPiece(color, ChessPiece.PieceType.KING);
        if (king.equals(board.getPiece(new ChessPosition(row, KING_STARTING_COL)))) {
            castleEligible.get(color).add(CastlePieces.KING);
        }
        if (rook.equals((board.getPiece(new ChessPosition(row, FIRST_ROW_COL))))) {
            castleEligible.get(color).add(CastlePieces.ROOK_1);
        }
        if (rook.equals((board.getPiece(new ChessPosition(row, LAST_ROW_COL))))) {
            castleEligible.get(color).add(CastlePieces.ROOK_8);
        }
    }

    SpecialMovesState(SpecialMovesState copyFrom) {
        lastMoveIfPawn = copyFrom.lastMoveIfPawn;       // immutable
        castleEligible = new EnumMap<>(ChessGame.TeamColor.class);
        for (var entry : copyFrom.castleEligible.entrySet()) {
            EnumSet<CastlePieces> copiedSet = EnumSet.noneOf(CastlePieces.class);
            copiedSet.addAll(entry.getValue());
            castleEligible.put(entry.getKey(), copiedSet);
        }
    }

    public void recordMove(ChessPiece piece, ChessMove move) {
        Objects.requireNonNull(piece, "piece");
        Objects.requireNonNull(move, "move");
        lastMoveIfPawn = piece.getPieceType().equals(ChessPiece.PieceType.PAWN) &&
                            ChessBoard.isStartingRow(move.getStartPosition(), piece) ? move : null;
        if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            castleEligible.get(piece.getTeamColor()).remove(CastlePieces.KING);
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
            if (ChessBoard.isStartingRow(move.getStartPosition(), piece)) {
                removeRookEligibility(piece.getTeamColor(), move.getStartPosition().getColumn());
            }
        }
        // if a rook was (potentially) captured, remove eligibility
        if (ChessBoard.isPromotionRank(move.getEndPosition(), piece.getTeamColor())) {
            removeRookEligibility(ChessGame.opponentColor(piece.getTeamColor()), move.getEndPosition().getColumn());
        }
    }

    private void removeRookEligibility(ChessGame.TeamColor color, int rookCol) {
        if (rookCol == FIRST_ROW_COL) {
            castleEligible.get(color).remove(CastlePieces.ROOK_1);
        }
        else if (rookCol == LAST_ROW_COL) {
            castleEligible.get(color).remove(CastlePieces.ROOK_8);
        }
    }

    /**
     * Assume the caller is asking about a pawn's movement
     * @param piece the piece being considered to make the en passant move
     * @param startPosition the position of that piece
     * @return null if no en passant is legal or the position of the en passant move
     */
    public ChessPosition getEnPassantPosition(ChessPiece piece, ChessPosition startPosition) {
        // legal if we are moving a pawn, the last move was by a pawn that moved two rows,
        // it ended up the same row as the given piece, and we are in adjacent columns
        ChessPosition enPassantPosition = null;
        if (lastMoveIfPawn != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int pawnRowsMoved = Math.abs(lastMoveIfPawn.getStartPosition().getRow() - lastMoveIfPawn.getEndPosition().getRow());
            int columnDiff = Math.abs(lastMoveIfPawn.getEndPosition().getColumn() - startPosition.getColumn());
            if (pawnRowsMoved == 2 && columnDiff == 1 && lastMoveIfPawn.getEndPosition().getRow() == startPosition.getRow()) {
                int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
                enPassantPosition = new ChessPosition(startPosition.getRow() + direction,
                                                    lastMoveIfPawn.getEndPosition().getColumn());
            }
        }
        return enPassantPosition;
    }

    public boolean isCastleLeftLegal(ChessPosition position, ChessPiece piece) {
        return ChessPiece.PieceType.KING.equals(piece.getPieceType()) &&
                ChessBoard.isStartingRow(position, piece) && position.getColumn() == KING_STARTING_COL &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.KING) &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.ROOK_1);
    }

    public boolean isCastleRightLegal(ChessPosition position, ChessPiece piece) {
        return ChessPiece.PieceType.KING.equals(piece.getPieceType()) &&
                ChessBoard.isStartingRow(position, piece) && position.getColumn() == KING_STARTING_COL &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.KING) &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.ROOK_8);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpecialMovesState that = (SpecialMovesState) o;
        return Objects.equals(lastMoveIfPawn, that.lastMoveIfPawn) && Objects.equals(castleEligible, that.castleEligible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastMoveIfPawn, castleEligible);
    }
}
