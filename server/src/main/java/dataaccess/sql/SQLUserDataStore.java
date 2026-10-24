package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class SQLUserDataStore extends AbstractSQLDataStore implements UserDAO {
    static final String TABLE_NAME = "user";
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (" +
            "id INT NOT NULL AUTO_INCREMENT, " +
            "username VARCHAR(64) NOT NULL, " +
            "password VARCHAR(64) NOT NULL, " +
            "email VARCHAR(64) NOT NULL, " +
            "PRIMARY KEY(id)," +
            "INDEX(username) " +
            ");";
    private static final String INSERT_USER_DATA =
            "INSERT INTO " + TABLE_NAME + " (username, password, email) VALUES (?, ?, ?);";
    private static final String QUERY_FOR_USERNAME =
            "SELECT * FROM " + TABLE_NAME + " WHERE username = ?;";
    private static final String QUERY_FOR_USERNAME_FOR_ID =
            "SELECT id FROM " + TABLE_NAME + " WHERE username = ?;";
    private static final String DELETE_AUTH_DATA =
            "DELETE FROM " + TABLE_NAME + " WHERE username = ?;";
    @SuppressWarnings("SqlWithoutWhere")
    private static final String EMPTY_TABLE = "DELETE FROM " + TABLE_NAME + ";";


    @Override
    protected String getCreateTableSql() {
        return CREATE_USER_TABLE;
    }

    @Override
    protected String getClearTableSql() {
        return EMPTY_TABLE;
    }

    public SQLUserDataStore(Supplier<Connection> connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public void addUserData(UserData userData) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(INSERT_USER_DATA)) {
            preparedStatement.setString(1, userData.username());
            preparedStatement.setString(2, userData.password());
            preparedStatement.setString(3, userData.email());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to add user data", ex);
        }
    }

    @Override
    public UserData getUserData(String userName) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_FOR_USERNAME)) {
            preparedStatement.setString(1, userName);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    var username = rs.getString("username");
                    var password = rs.getString("password");
                    var email = rs.getString("email");
                    return new UserData(username, password, email);
                }
                else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve user data for user: " + userName, ex);
        }
    }

    public int getIdForUsername(String userName) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(QUERY_FOR_USERNAME_FOR_ID)) {
            preparedStatement.setString(1, userName);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                else {
                    return -1;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve ID for user: " + userName, ex);
        }
    }

    @Override
    public void deleteUserData(UserData userData) {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(DELETE_AUTH_DATA)) {
            preparedStatement.setString(1, userData.username());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete user data for user: " + userData.username(), ex);
        }
    }
}
