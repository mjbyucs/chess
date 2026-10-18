package chess;

import java.util.*;
import java.util.stream.Stream;

/*
 * Class that implements move semantics for each chess piece and generates pseudo-legal moves.
 * Final move validation is the responsibility of ChessGame.
 */
public class MoveGenerator {

    /**
     * Return all valid moves for the piece located at the given position.
     * @param board the ChessBoard from which to generate moves
     * @param position the ChessPosition from which to generate moves
     * @return list of valid moves
     */
    public static Collection<ChessMove> generateMoves(ChessBoard board, ChessPiece piece, ChessPosition position) {
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            return pawnMoves(board, position, piece, piece.getTeamColor());
        }
        else {
            return nonPawnMoves(board, position, piece, piece.getTeamColor());
        }
    }

    /**
     * Return the positions that can be attacked by the piece located at the given position.
     * Unlike ordinary pawn movement, pawn attacks are to diagonal squares. These are pseudo-legal
     * attacks and do not account for whether there is a piece in the location being attacked.
     * @param board the ChessBoard from which to generate attacks
     * @param position the ChessPosition from which to generate attacks
     * @return list of valid attack moves
     */
    public static Collection<ChessMove> generateAttacks(ChessBoard board, ChessPiece piece, ChessPosition position) {
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            return pawnAttacks(board, position, piece.getTeamColor());
        }
        else {
            // non-pawns have the same attack positions as move positions
            return nonPawnMoves(board, position, piece, piece.getTeamColor());
        }
    }

    private static Collection<ChessMove> nonPawnMoves(ChessBoard board, ChessPosition position,
                                                      ChessPiece piece, ChessGame.TeamColor color) {
        List<ChessMove> moves = new ArrayList<>();
        boolean canSlide = CAN_SLIDE.contains(piece.getPieceType());

        // get the list of directions a piece can move and generate all new positions from the current one
        for (ChessPosition pos : PIECE_DIRECTIONS.get(piece.getPieceType())) {
            ChessPosition newPos = position.add(pos);
            while (newPos.isValid() && board.canMoveTo(newPos, color)) {
                moves.add(new ChessMove(position, newPos));
                // stop after the first move if the piece can't slide
                // or stop if we've encountered an opponent piece
                if (!canSlide || board.isOpponentPiece(newPos, color)) {
                    break;
                }
                newPos = newPos.add(pos);
            }
        }
        return moves;
    }

    // handle all the cases for pawn
    private static List<ChessMove> pawnMoves(ChessBoard board, ChessPosition position,
                                             ChessPiece piece, ChessGame.TeamColor color) {
        List<ChessPosition> newPositions = new ArrayList<>();

        int moveDir = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        ChessPosition direction = new ChessPosition(moveDir, 0);
        ChessPosition moveOne = position.add(direction);
        if (moveOne.isValid() && board.isEmpty(moveOne)) {
            newPositions.add(moveOne);
            if (ChessBoard.isStartingRow(position, piece)) {
                ChessPosition moveTwo = moveOne.add(direction);
                if (moveTwo.isValid() && board.isEmpty(moveTwo)) {
                    newPositions.add(moveTwo);
                }
            }
        }
        // check capture
        ChessPosition leftDiag = position.add(new ChessPosition(moveDir, -1));
        ChessPosition rightDiag = position.add(new ChessPosition(moveDir, 1));
        if (leftDiag.isValid() && board.isOpponentPiece(leftDiag, color)) {
            newPositions.add(leftDiag);
        }
        if (rightDiag.isValid() && board.isOpponentPiece(rightDiag, color)) {
            newPositions.add(rightDiag);
        }

        return convertCandidatePositionsToChessMoves(position, color, newPositions);
    }

    private static List<ChessMove> convertCandidatePositionsToChessMoves(ChessPosition position,
                                                                         ChessGame.TeamColor color,
                                                                         List<ChessPosition> newPositions) {
        List<ChessMove> moves = new ArrayList<>();

        // convert positions into moves while also checking for reaching the promotion row
        for (ChessPosition pos : newPositions) {
            if (ChessBoard.isPromotionRank(pos, color)) {
                moves.add(new ChessMove(position, pos, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(position, pos, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(position, pos, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(position, pos, ChessPiece.PieceType.KNIGHT));
            }
            else {
                moves.add(new ChessMove(position, pos));
            }
        }
        return moves;
    }


    // handle all the cases for pawn
    private static List<ChessMove> pawnAttacks(ChessBoard board, ChessPosition position,
                                                ChessGame.TeamColor color) {
        List<ChessPosition> newPositions = new ArrayList<>();

        int moveDir = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        // capture positions
        ChessPosition leftDiag = position.add(new ChessPosition(moveDir, -1));
        ChessPosition rightDiag = position.add(new ChessPosition(moveDir, 1));
        if (leftDiag.isValid() && board.canMoveTo(leftDiag, color)) {
            newPositions.add(leftDiag);
        }
        if (rightDiag.isValid() && board.canMoveTo(rightDiag, color)) {
            newPositions.add(rightDiag);
        }

        return convertCandidatePositionsToChessMoves(position, color, newPositions);
    }

    private static final Set<ChessPiece.PieceType> CAN_SLIDE = Set.of(
            ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP
    );
    private static final List<ChessPosition> ORTHOGONAL = List.of(
            new ChessPosition(1, 0), new ChessPosition(-1, 0),
            new ChessPosition(0, 1), new ChessPosition(0, -1)
    );
    private static final List<ChessPosition> DIAGONAL = List.of(
            new ChessPosition(1, 1), new ChessPosition(-1, 1),
            new ChessPosition(1, -1), new ChessPosition(-1, -1)
    );
    private static final List<ChessPosition> KNIGHT_MOVES = List.of(
            new ChessPosition(2, 1), new ChessPosition(2, -1),
            new ChessPosition(-2, 1), new ChessPosition(-2, -1),
            new ChessPosition(1, 2), new ChessPosition(1, -2),
            new ChessPosition(-1, 2), new ChessPosition(-1, -2)
    );
    private static final List<ChessPosition> ALL_DIRS = Stream.concat(ORTHOGONAL.stream(), DIAGONAL.stream()).toList();
    private static final Map<ChessPiece.PieceType, List<ChessPosition>> PIECE_DIRECTIONS = Map.of(
            ChessPiece.PieceType.KING, ALL_DIRS,
            ChessPiece.PieceType.QUEEN, ALL_DIRS,
            ChessPiece.PieceType.ROOK, ORTHOGONAL,
            ChessPiece.PieceType.BISHOP, DIAGONAL,
            ChessPiece.PieceType.KNIGHT, KNIGHT_MOVES,
            ChessPiece.PieceType.PAWN, List.of()            // handle all pawn moves specially
    );
}
