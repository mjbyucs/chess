package chess.movers;

import chess.*;

import java.util.Collection;
import java.util.List;

public class KingMover extends AbstractPieceMover {
    @Override
    protected int[] getMoveDirections(ChessGame.TeamColor color) {
        return new int[] {
                -1, 1, boardDimension, -boardDimension,
                boardDimension - 1, boardDimension + 1,
                -boardDimension - 1, -boardDimension + 1
        };
    }

    @Override
    protected boolean canMoveMultiple() {
        return false;
    }

    @Override
    protected Collection<ChessMove> getSpecialMoves(ChessBoard board, int pieceIndex,
                                                    ChessGame.TeamColor color) {
        // todo: implement castle semantics
        return List.of();
    }
}
