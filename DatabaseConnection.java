import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to the MySQL database.
 * Uses credentials from {@link DatabaseConfig}.
 */
public class DatabaseConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. Add mysql-connector-j JAR to the classpath.", e);
        }
    }

    /**
     * Returns a new connection to the configured database.
     * Callers are responsible for closing the connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }
}
