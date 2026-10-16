package server;

import chess.*;

public class ServerMain {
    private enum StorageType {
        MEMORY,
        DATABASE
    }
    public static void main(String[] args) {
        StorageType storage = StorageType.MEMORY;
        var port = 8080;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "--mem" -> storage = StorageType.MEMORY;
                case "--db" -> storage = StorageType.DATABASE;
                case null, default -> throw new IllegalArgumentException("Unrecognized argument: [" + args[i] + "]");
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
