package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SQLTestHelper {
    private final AtomicBoolean failConnections = new AtomicBoolean(false);

    public Connection getConnection() {
        Connection connection = DatabaseManager.getConnection();

        if (failConnections.get()) {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return connection;
    }

    void enableConnectionFailures() {
        failConnections.set(true);
    }

    void disableConnectionFailures() {
        failConnections.set(false);
    }
}
