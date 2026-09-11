package chess.movers;

import chess.*;

public class QueenMover extends AbstractPieceMover {
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
        return true;
    }
}
