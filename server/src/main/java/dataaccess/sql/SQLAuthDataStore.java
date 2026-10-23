package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class SQLAuthDataStore implements AuthDAO {
    private static final String TABLE_NAME = "authorization";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (" +
            "id INT NOT NULL AUTO_INCREMENT, " +
            "authToken VARCHAR(64) NOT NULL, " +
            "userId INT NOT NULL, " +
            "PRIMARY KEY(id)," +
            "INDEX(authToken), " +
            "CONSTRAINT fk_authorization_user" +
            "        FOREIGN KEY (userId)" +
            "        REFERENCES " + SQLUserDataStore.TABLE_NAME + "(id)" +
            "        ON DELETE CASCADE" +
            ");";
    private static final String INSERT_AUTH_DATA = "INSERT INTO " + TABLE_NAME + " (authToken, userId) VALUES (?, ?);";
    private static final String QUERY_FOR_USERNAME = "SELECT u.username FROM " + TABLE_NAME + " a " +
            "JOIN " + SQLUserDataStore.TABLE_NAME + " u ON a.userId = u.id " +
            "WHERE authToken = ?;";
    private static final String DELETE_AUTH_DATA = "DELETE FROM " + TABLE_NAME + " WHERE authToken = ?;";
    @SuppressWarnings("SqlWithoutWhere")
    private static final String EMPTY_TABLE = "DELETE FROM " + TABLE_NAME + ";";

    private final Supplier<Connection> connectionProvider;
    private final SQLUserDataStore userDataStore;

    public SQLAuthDataStore(Supplier<Connection> connectionProvider, SQLUserDataStore userDataStore) {
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
    public void addAuthData(AuthData authData) {
        int userId = userDataStore.getIdForUsername(authData.username());
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(INSERT_AUTH_DATA)) {
            preparedStatement.setString(1, authData.authToken());
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to add authorization data", ex);
        }
    }

    @Override
    public AuthData getAuthData(String authToken) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_FOR_USERNAME)) {
            preparedStatement.setString(1, authToken);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    var username = rs.getString("username");
                    return new AuthData(authToken, username);
                }
                else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve authorization data for authToken: " + authToken, ex);
        }
    }

    @Override
    public void deleteAuthData(AuthData authData) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(DELETE_AUTH_DATA)) {
            preparedStatement.setString(1, authData.authToken());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete authorization data for authToken: " + authData.authToken(), ex);
        }
    }

    @Override
    public void clearDatabase() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(EMPTY_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to empty authorization table", ex);
        }
    }

}
