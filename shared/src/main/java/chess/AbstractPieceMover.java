package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractPieceMover implements PieceMover {
    protected int boardDimension = ChessBoard.getBoardDimension();
    protected abstract int[] getMoveDirections(ChessGame.TeamColor color);
    protected abstract boolean canMoveMultiple();

    @Override
    public Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition myPosition) {
        // assume there is a piece at the current position. Hard to know the direction otherwise.
        ChessGame.TeamColor color = board.getPiece(myPosition).getTeamColor();
        int[] moveDirections = getMoveDirections(color);
        int curIdx = ChessBoard.positionToIndex(myPosition);

        List<Integer> candidateIndeces = createCandidateIndeces(board, moveDirections, curIdx);
        // filter positions that aren't legal
        candidateIndeces.removeIf(idx -> !ChessBoard.isIndexOnboard(idx) || isMyPieceAtIndex(board, color, idx));

        List<ChessMove> moves = new ArrayList<>();
        for (int idx : candidateIndeces) {
            moves.add(new ChessMove(myPosition, ChessBoard.indexToPosition(idx)));
        }

        // check to see if there are special moves to include
        moves.addAll(getSpecialMoves(board, curIdx, color));

        return moves;
    }

    private List<Integer> createCandidateIndeces(ChessBoard board, int[] moveDirections, int curIdx) {
        List<Integer> candidateIndeces = new ArrayList<>();
        for (int d : moveDirections) {
           int nextIdx = curIdx + d;
           candidateIndeces.add(nextIdx);
           // to keep moving we need pieces that slide (Q, R, B), still be on the board, and be on an empty square
           while (canMoveMultiple() && ChessBoard.isIndexOnboard(nextIdx) && isEmptyIndex(board, nextIdx)) {
               nextIdx = nextIdx + d;
               candidateIndeces.add(nextIdx);
           }
        }
        return candidateIndeces;
    }

    protected ChessPosition indexToPosition(int index) {
        return ChessBoard.indexToPosition(index);
    }

    protected boolean isEmptyIndex(ChessBoard board, int index) {
        return board.getPieceAtIndex(index) == null;
    }

    protected boolean isMyPieceAtIndex(ChessBoard board, ChessGame.TeamColor color, int idx) {
        ChessPiece piece = board.getPieceAtIndex(idx);
        return piece != null && piece.getTeamColor() == color;
    }

    protected boolean isInOriginalRow(int pieceIndex, ChessPiece.PieceType pieceType, ChessGame.TeamColor color) {
        return ChessBoard.isInOriginalRow(pieceIndex, pieceType, color);
    }

    // the base class provides behavior of no special moves (applies only to pawn and king)
    protected Collection<ChessMove> getSpecialMoves(ChessBoard board, int pieceIndex,
                                                    ChessGame.TeamColor color) {
        return List.of();
    }
}
