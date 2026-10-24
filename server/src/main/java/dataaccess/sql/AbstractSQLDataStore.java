package dataaccess.sql;

import dataaccess.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

abstract class AbstractSQLDataStore {
    protected final Supplier<Connection> connectionProvider;

    abstract protected String getCreateTableSql();
    abstract protected String getClearTableSql();

    protected AbstractSQLDataStore(Supplier<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
        createTable();
    }

    private void createTable() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(getCreateTableSql())) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    public void clearDatabase() {
        try (var conn = connectionProvider.get();
             var preparedStatement = conn.prepareStatement(getClearTableSql())) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to empty user table", ex);
        }
    }

}
