import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections with automatic MySQL → SQLite failover.
 *
 * On each {@link #getConnection()} call the class tries MySQL first.
 * If MySQL is unreachable it transparently falls back to the local
 * SQLite database file configured in {@link DatabaseConfig#SQLITE_URL}.
 */
public class DatabaseConnection {

    public enum Mode { MYSQL, SQLITE }

    private static volatile Mode activeMode = Mode.MYSQL;

    static {
        // Load both JDBC drivers once.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC driver not found – MySQL will be unavailable.");
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] SQLite JDBC driver not found – SQLite fallback will be unavailable.");
        }
    }

    /**
     * Returns a connection to the currently active database.
     * Tries MySQL first; on failure falls back to SQLite.
     */
    public static Connection getConnection() throws SQLException {
        // Always try MySQL first so we auto-recover when it comes back.
        try {
            Connection conn = DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD);
            if (activeMode == Mode.SQLITE) {
                System.out.println("[DatabaseConnection] MySQL is back online – switching from SQLite.");
            }
            activeMode = Mode.MYSQL;
            return conn;
        } catch (SQLException mysqlEx) {
            // MySQL unavailable – fall back to SQLite.
            if (activeMode == Mode.MYSQL) {
                System.out.println("[DatabaseConnection] MySQL unavailable (" + mysqlEx.getMessage()
                        + ") – falling back to SQLite.");
            }
            activeMode = Mode.SQLITE;
            return DriverManager.getConnection(DatabaseConfig.SQLITE_URL);
        }
    }

    /**
     * Returns a direct MySQL connection (bypassing failover).
     * Used only by the sync manager to test connectivity and mirror data.
     * Returns {@code null} if MySQL is unreachable.
     */
    public static Connection getMySQLConnection() {
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Returns a direct SQLite connection (bypassing failover).
     * Used only by the sync manager.
     */
    public static Connection getSQLiteConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.SQLITE_URL);
    }

    /** Returns the currently active database mode. */
    public static Mode getActiveMode() {
        return activeMode;
    }

    /** Returns {@code true} when the app is connected to MySQL. */
    public static boolean isOnline() {
        return activeMode == Mode.MYSQL;
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }
}
