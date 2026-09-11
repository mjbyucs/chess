package chess.movers;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMover extends AbstractPieceMover {
    @Override
    protected int[] getMoveDirections(ChessGame.TeamColor color) {
        // pawn moves are special so handle them all in special moves
        // we can't even handle the move one forward here because if we've
        // reached the end of the board we need to supply a promotion piece
        return new int[] {};
    }

    @Override
    protected boolean canMoveMultiple() {
        return false;
    }

    private boolean indexHasOpponentPiece(ChessBoard board, int pieceIndex,
                                          ChessGame.TeamColor color) {
        return !isEmptyIndex(board, pieceIndex) && !isMyPieceAtIndex(board, color, pieceIndex);
    }

    @Override
    protected Collection<ChessMove> getSpecialMoves(ChessBoard board, int pieceIndex,
                                                    ChessGame.TeamColor color) {
        int direction = color == ChessGame.TeamColor.WHITE ? boardDimension : -boardDimension;
        List<Integer> candidateIndeces = new ArrayList<>();

        // check move straight forward
        int oneStepForward = pieceIndex + direction;
        if (isEmptyIndex(board, oneStepForward)) {
            candidateIndeces.add(oneStepForward);
            if (isInOriginalRow(pieceIndex, ChessPiece.PieceType.PAWN, color)) {
                int twoStepsForward = oneStepForward + direction;
                if (isEmptyIndex(board, twoStepsForward)) {
                    candidateIndeces.add(twoStepsForward);
                }
            }
        }

        // check capture
        if (indexHasOpponentPiece(board, pieceIndex + direction + 1, color)) {
            candidateIndeces.add(pieceIndex + direction + 1);
        }
        if (indexHasOpponentPiece(board, pieceIndex + direction - 1, color)) {
            candidateIndeces.add(pieceIndex + direction - 1);
        }
        if (isInOriginalRow(pieceIndex, ChessPiece.PieceType.PAWN, color)) {
            // todo: implement En Passant move
        }

        List<ChessMove> moves = new ArrayList<>();
        for (int idx : candidateIndeces) {
            // see if we need to promote by checking if we are in the opponent's back row
            if (isInOriginalRow(idx, ChessPiece.PieceType.QUEEN,
                                color == ChessGame.TeamColor.WHITE ?
                                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE)) {
                // seems like we ought to ask the user rather than just pick!!!
                ChessPosition startPos = indexToPosition(pieceIndex);
                ChessPosition endPos = indexToPosition(idx);
                moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.KNIGHT));
            }
            else {
                moves.add(new ChessMove(indexToPosition(pieceIndex), indexToPosition(idx)));
            }
        }

        return moves;
    }

}

