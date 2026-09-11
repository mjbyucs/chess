package chess.movers;

import chess.*;

public class KnightMover extends AbstractPieceMover {
    @Override
    protected int[] getMoveDirections(ChessGame.TeamColor color) {
        int dim2 = boardDimension * 2;
        return new int[] {
                dim2 + 1, dim2 -1, -dim2 + 1, -dim2 -1,
                boardDimension - 2, boardDimension + 2,
                -boardDimension - 2, -boardDimension + 2
        };
    }

    @Override
    protected boolean canMoveMultiple() {
        return false;
    }
}
