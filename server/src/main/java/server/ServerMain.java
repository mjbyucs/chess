package server;

import chess.*;

public class ServerMain {
    private static enum StorageType {
        MEMORY,
        DATABASE
    }
    public static void main(String[] args) {
        StorageType storage = StorageType.MEMORY;
        var port = 8080;
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            else if ("--mem".equals(args[i])) {
                storage = StorageType.MEMORY;
            }
            else if ("--db".equals(args[i])) {
                storage = StorageType.DATABASE;
            }
            else {
                throw new IllegalArgumentException("Unrecognized argument: [" + args[i] + "]");
            }
        }

        Server server = storage == StorageType.MEMORY ? ApplicationComposer.createMemoryServer() :
                                                        ApplicationComposer.createDatabaseServer();
        server.run(port);

        System.out.println("♕ 240 Chess Server running on port " + port);

//
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
