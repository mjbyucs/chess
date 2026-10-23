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
            "username VARCHAR(64) NOT NULL, " +
            "PRIMARY KEY(id)," +
            "INDEX(authToken) " +
            ");";
    private static final String INSERT_AUTH_DATA = "INSERT INTO " + TABLE_NAME + " (authToken, username) VALUES (?, ?);";
    private static final String QUERY_FOR_USERNAME = "SELECT username FROM " + TABLE_NAME + " WHERE authToken = ?;";
    private static final String DELETE_AUTH_DATA = "DELETE FROM " + TABLE_NAME + " WHERE authToken = ? AND username = ?;";
    private static final String EMPTY_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private final Supplier<Connection> connectionProvider;

    public SQLAuthDataStore(Supplier<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
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
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(INSERT_AUTH_DATA)) {
            preparedStatement.setString(1, authData.authToken());
            preparedStatement.setString(2, authData.username());
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
            preparedStatement.setString(2, authData.username());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete authorization data for authToken: " + authData.authToken(), ex);
        }
    }

    @Override
    public void deleteDatabase() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(EMPTY_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to empty authorization table", ex);
        }
    }
}
