package chess.movers;

import chess.*;

public class RookMover extends AbstractPieceMover {
    @Override
    protected int[] getMoveDirections(ChessGame.TeamColor color) {
        return new int[] {-1, 1, boardDimension, -boardDimension};
    }

    @Override
    protected boolean canMoveMultiple() {
        return true;
    }
}
