package chess;

import java.util.*;

/**
 * Class that keeps state about movement of pawns, rooks, and kings that can report if
 * en passant or castling are legal moves. The assumption is that this state is paired
 * with a particular board so if a board is copied, so should its state be copied.
 */
public class SpecialMovesState {
    private static final int KING_STARTING_COL = 5;
    private ChessMove lastMoveIfPawn;
    private final Map<ChessGame.TeamColor, Set<CastlePieces>> castleEligible;

    private enum CastlePieces {
        KING, ROOK_1, ROOK_8, DUMMY     // dummy piece makes logic work nicer so we don't deal with nulls
    }

    SpecialMovesState() {
        lastMoveIfPawn = null;
        castleEligible = Map.of(
                ChessGame.TeamColor.WHITE, EnumSet.allOf(CastlePieces.class),
                ChessGame.TeamColor.BLACK, EnumSet.allOf(CastlePieces.class)
        );
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
        lastMoveIfPawn = piece.getPieceType().equals(ChessPiece.PieceType.PAWN) ? move : null;
        if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            castleEligible.get(piece.getTeamColor()).remove(CastlePieces.KING);
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
            if (ChessBoard.isStartingRow(move.getStartPosition(), piece)) {
                int startCol = move.getStartPosition().getColumn();
                CastlePieces ineligiblePiece = startCol == 1 ? CastlePieces.ROOK_1 :
                        (startCol == 8 ? CastlePieces.ROOK_8 : CastlePieces.DUMMY);
                castleEligible.get(piece.getTeamColor()).remove(ineligiblePiece);
            }
        }
    }

    /**
     * Assume the caller is asking about a pawn's movement
     * @param piece the piece being considered to make the en passant move
     * @param startPosition the position of that piece
     * @return null if no en passant is legal or the position of the en passant move
     */
    public ChessPosition getEnPassantPosition(ChessPiece piece, ChessPosition startPosition) {
        // legal if we are moving a pawn, the last move was by a pawn that moved two columns,
        // it ended up the same row as the given piece, and we are in adjacent columns
        ChessPosition enPassantPosition = null;
        if (lastMoveIfPawn != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int pawnRowsMoved = Math.abs(lastMoveIfPawn.getStartPosition().getRow() - lastMoveIfPawn.getEndPosition().getRow());
            int columnDiff = Math.abs(lastMoveIfPawn.getEndPosition().getColumn() - startPosition.getColumn());
            if (pawnRowsMoved == 2 && columnDiff == 1) {
                int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
                enPassantPosition = new ChessPosition(startPosition.getRow() + direction,
                                                    lastMoveIfPawn.getEndPosition().getColumn());
            }
        }
        return enPassantPosition;
    }

    public boolean isCastleLeftLegal(ChessPosition position, ChessPiece piece) {
        return ChessBoard.isStartingRow(position, piece) && position.getColumn() == KING_STARTING_COL &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.KING) &&
                castleEligible.get(piece.getTeamColor()).contains(CastlePieces.ROOK_1);
    }

    public boolean isCastleRightLegal(ChessPosition position, ChessPiece piece) {
        return ChessBoard.isStartingRow(position, piece) && position.getColumn() == KING_STARTING_COL &&
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
