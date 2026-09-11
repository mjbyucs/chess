package chess.movers;

import chess.*;

public class BishopMover extends AbstractPieceMover {
    @Override
    protected int[] getMoveDirections(ChessGame.TeamColor color) {
        return new int[] {boardDimension - 1, boardDimension + 1, -boardDimension - 1, -boardDimension + 1};
    }

    @Override
    protected boolean canMoveMultiple() {
        return true;
    }
}
