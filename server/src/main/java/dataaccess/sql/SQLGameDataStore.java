package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("SqlResolve")
public class SQLGameDataStore implements GameDAO {
    private static final String TABLE_NAME = "game";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (" +
            "id INT NOT NULL AUTO_INCREMENT, " +
            "gameName VARCHAR(64) NOT NULL, " +
            "whiteUserId INT NULL, " +
            "blackUserId INT NULL, " +
            "gameState VARCHAR(4096) NOT NULL, " +
            "PRIMARY KEY(id)," +
            "INDEX(gameName), " +
            "CONSTRAINT fk_white_user" +
            "        FOREIGN KEY (whiteUserId)" +
            "        REFERENCES " + SQLUserDataStore.TABLE_NAME + "(id)" +
            "        ON DELETE CASCADE," +
            "CONSTRAINT fk_black_user" +
            "        FOREIGN KEY (blackUserId)" +
            "        REFERENCES " + SQLUserDataStore.TABLE_NAME + "(id)" +
            "        ON DELETE CASCADE" +
            ");";
    private static final String INSERT_GAME_DATA = "INSERT INTO " + TABLE_NAME + " " +
            "(gameName, gameState) VALUES (?, ?);";
    private static final String UPDATE_PLAYER_DATA = "UPDATE " + TABLE_NAME + " " +
            "SET %s = ? WHERE id = ?";
    private static final String QUERY_BY_GAMEID = "SELECT g.id, g.gameName, w.username as whitePlayerName, b.username as blackPlayerName, g.gameState " +
            "FROM " + TABLE_NAME + " g " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " w ON g.whiteUserId = w.id " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " b ON g.blackUserId = b.id " +
            "WHERE g.id = ?;";
    private static final String QUERY_BY_GAMENAME = "SELECT g.id, g.gameName, w.username as whitePlayerName, b.username as blackPlayerName, g.gameState " +
            "FROM " + TABLE_NAME + " g " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " w ON g.whiteUserId = w.id " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " b ON g.blackUserId = b.id " +
            "WHERE g.gameName = ?;";
    private static final String QUERY_ALL_GAMES = "SELECT g.id, g.gameName, w.username as whitePlayerName, b.username as blackPlayerName, g.gameState " +
            "FROM " + TABLE_NAME + " g " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " w ON g.whiteUserId = w.id " +
            "LEFT JOIN " + SQLUserDataStore.TABLE_NAME + " b ON g.blackUserId = b.id " +
            ";";
    private static final String DELETE_GAME_DATA = "DELETE FROM " + TABLE_NAME + " WHERE id = ?;";
    @SuppressWarnings("SqlWithoutWhere")
    private static final String EMPTY_TABLE = "DELETE FROM " + TABLE_NAME + ";";

    private final Supplier<Connection> connectionProvider;
    private final SQLUserDataStore userDataStore;
    private final Gson gson = new Gson();

    public SQLGameDataStore(Supplier<Connection> connectionProvider, SQLUserDataStore userDataStore) {
        this.connectionProvider = connectionProvider;
        this.userDataStore = userDataStore;
        createTable();
    }

    private void createTable() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(CREATE_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    @Override
    public int addGame(@NotNull String gameName, @NotNull ChessGame chessGame) throws AlreadyTakenException {
        if (getGameByName(gameName) != null) {
            throw new AlreadyTakenException();
        }
        String serializedGameState = gson.toJson(chessGame);
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(INSERT_GAME_DATA, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, gameName);
            preparedStatement.setString(2, serializedGameState);
            preparedStatement.executeUpdate();

            var rs = preparedStatement.getGeneratedKeys();
            var id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to add game data", ex);
        }
    }

    @Override
    public GameData getGameByName(String gameName) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_BY_GAMENAME)) {
            preparedStatement.setString(1, gameName);
            try (var rs = preparedStatement.executeQuery()) {
                return getGameDataFromResultSet(rs);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve game data for name: " + gameName, ex);
        }
    }

    @Override
    public GameData getGameByID(int gameID) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_BY_GAMEID)) {
            preparedStatement.setInt(1, gameID);
            try (var rs = preparedStatement.executeQuery()) {
                return getGameDataFromResultSet(rs);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve game data for ID: " + gameID, ex);
        }
    }

    @Nullable
    private GameData getGameDataFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            var gameId = rs.getInt("id");
            var gameName = rs.getString("gameName");
            var whitePlayerName = rs.getString("whitePlayerName");
            var blackPlayerName = rs.getString("blackPlayerName");
            var gameState = rs.getString("gameState");
            ChessGame chessGame = gson.fromJson(gameState, ChessGame.class);
            return new GameData(gameId, whitePlayerName, blackPlayerName, gameName, chessGame);
        }
        else {
            return null;
        }
    }

    @Override
    public void updatePlayerNames(int gameID, String whitePlayerName, String blackPlayerName) {
        if (whitePlayerName != null) {
            int whitePlayerId = getPlayerIdFromName(whitePlayerName);
            updatePlayerID(gameID, "whiteUserId", whitePlayerId);
        }
        if (blackPlayerName != null) {
            int blackPlayerId = getPlayerIdFromName(blackPlayerName);
            updatePlayerID(gameID, "blackUserId", blackPlayerId);
        }
    }

    private int getPlayerIdFromName(String playerName) {
        int playerId = userDataStore.getIdForUsername(playerName);
        if (playerId < 0) {
            throw new IllegalArgumentException("Invalid player");
        }
        return playerId;
    }

    private void updatePlayerID(int gameID, String playerColumn, int playerID) {
        String sql = String.format(UPDATE_PLAYER_DATA, playerColumn);
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, playerID);
            preparedStatement.setInt(2, gameID);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to update player for game " + gameID, ex);
        }
    }

    @Override
    public List<GameData> getAllGames() {
        List<GameData> allGames = new ArrayList<>();
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_ALL_GAMES)) {
            try (var rs = preparedStatement.executeQuery()) {
                GameData gameData;
                while((gameData = getGameDataFromResultSet(rs)) != null) {
                    allGames.add(gameData);
                }
                return allGames;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve game data", ex);
        }
    }

    @Override
    public void deleteGame(int gameID) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(DELETE_GAME_DATA)) {
            preparedStatement.setInt(1, gameID);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete game " + gameID, ex);
        }
    }

    @Override
    public void clearDatabase() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(EMPTY_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to empty game table", ex);
        }
    }

}
