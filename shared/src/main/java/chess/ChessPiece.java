package chess;

import chess.movers.*;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType pieceType;
    private final PieceMover pieceMover;

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
        this.pieceMover = getMover(type);
    }

        private PieceMover getMover(ChessPiece.PieceType type) {
            return switch (type) {
                case KING -> new KingMover();
                case QUEEN -> new QueenMover();
                case ROOK -> new RookMover();
                case BISHOP -> new BishopMover();
                case KNIGHT -> new KnightMover();
                case PAWN -> new PawnMover();
            };
        }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     *
     * Given a board configuration, this method returns all the moves a specific piece can
     * legally make independent of whose turn it is or if the King is being attacked.
     * It considers the edges of the board and the location of both enemy and friendly pieces.
     *
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return pieceMover.generateMoves(board, myPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceType);
    }
}
