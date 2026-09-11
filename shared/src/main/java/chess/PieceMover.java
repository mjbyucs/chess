package chess;

import java.util.Collection;

public interface PieceMover {
    Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition myPosition);
}
