package server;

public class ServerMain {
    private static final String DEVELOPMENT_DATABASE_PROPERTIES = "db-dev.properties";

    public static void main(String[] args) {
        DependencyComposer.StorageType storage = DependencyComposer.StorageType.MEMORY;
        var port = 8080;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p" -> port = Integer.parseInt(args[++i]);
                case "--mem" -> storage = DependencyComposer.StorageType.MEMORY;
                case "--db" -> storage = DependencyComposer.StorageType.DATABASE;
                case null, default -> throw new IllegalArgumentException("Unrecognized argument: [" + args[i] + "]");
            }
        }

        DependencyComposer dependencyComposer = new DependencyComposer(storage, DEVELOPMENT_DATABASE_PROPERTIES);
        Server server = new Server(dependencyComposer);
        server.run(port);

        System.out.println("♕ 240 Chess Server running on port " + port);

//
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
