package chess;

import java.util.*;
import java.util.stream.Stream;

/*
 * Class that implements move semantics for each chess piece and generates moves
 */
public class MoveGenerator {

    public static Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition position) {
        // find out what's here
        if (board.isEmpty(position)) {
            return List.of();
        }
        ChessPiece piece = board.getPiece(position);
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor color = piece.getTeamColor();
        boolean canSlide = CAN_SLIDE.contains(type);

        List<ChessMove> moves = new ArrayList<>();
        // get the list of directions a piece can move and generate all new positions from the current one
        for (ChessPosition pos : PIECE_DIRECTIONS.get(type)) {
            ChessPosition newPos = position.add(pos);
            if (newPos.isValid() && board.canMoveTo(newPos, color)) {
                ChessPosition prevPos;
                do {
                    prevPos = newPos;
                    moves.add(new ChessMove(position, newPos));
                    newPos = newPos.add(pos);
                } while (canSlide && newPos.isValid() &&            // stops after first iteration if the piece can't slide
                        board.canMoveTo(newPos, color) &&          // can't move onto another of my pieces
                        !board.isOpponentPiece(prevPos, color));    // can't slide beyond an opponent piece
            }
        }
        
        moves.addAll(generateSpecialMoves(board, position, piece, color));

        return moves;
    }

    private static Collection<ChessMove> generateSpecialMoves(ChessBoard board, ChessPosition position,
                                                              ChessPiece piece, ChessGame.TeamColor color) {
        return switch (piece.getPieceType()) {
            case KING -> kingSpecialMoves(board, position, piece, color);
            case PAWN -> pawnSpecialMoves(board, position, piece, color);
            default -> List.of();
        };
    }

    private static List<ChessMove> kingSpecialMoves(ChessBoard board, ChessPosition position, ChessPiece piece,
                                                    ChessGame.TeamColor color) {
//            // implement castling logic
        // TODO: castling logic
        return List.of();
    }

    private static List<ChessMove> pawnSpecialMoves(ChessBoard board, ChessPosition position, ChessPiece piece,
                                                    ChessGame.TeamColor color) {
        // handle all the special cases for pawn
        List<ChessPosition> newPositions = new ArrayList<>();

        int moveDir = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        ChessPosition direction = new ChessPosition(moveDir, 0);
        ChessPosition moveOne = position.add(direction);
        if (moveOne.isValid() && board.isEmpty(moveOne)) {
            newPositions.add(moveOne);
            if (board.isStartingRow(position, piece)) {
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
        // todo: implement En Passant move

        List<ChessMove> moves = new ArrayList<>();

        // convert positions into moves while also checking for reaching the promotion row
        for (ChessPosition pos : newPositions) {
            if (board.isPromotionRank(pos, color)) {
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
