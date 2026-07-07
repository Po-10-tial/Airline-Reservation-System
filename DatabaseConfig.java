/**
 * Centralized database configuration constants.
 * Update the values below to match your MySQL server settings.
 */
public class DatabaseConfig {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/airline_reservation_db";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "1234567890";

    /** SQLite fallback database — embedded file in the project directory. */
    public static final String SQLITE_URL = "jdbc:sqlite:airline_backup.db";

    private DatabaseConfig() {
        // Prevent instantiation
    }
}
