import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages the embedded SQLite database used as a failsafe when MySQL is unavailable.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Create the SQLite schema on first run</li>
 *   <li>Mirror data from MySQL → SQLite (warm cache)</li>
 *   <li>Sync data from SQLite → MySQL on reconnection</li>
 *   <li>Run a background timer to detect MySQL recovery</li>
 * </ul>
 */
public class SqliteManager {

    private static final int SYNC_INTERVAL_MS = 30_000; // 30 seconds
    private static Timer syncTimer;

    // Listener interface for mode-change notifications (used by the UI).
    public interface ModeChangeListener {
        void onModeChanged(DatabaseConnection.Mode newMode);
    }

    private static volatile ModeChangeListener modeChangeListener;

    public static void setModeChangeListener(ModeChangeListener listener) {
        modeChangeListener = listener;
    }

    // ======================================================================
    // Initialization
    // ======================================================================

    /**
     * Call once at application startup.
     * Creates the SQLite schema, mirrors MySQL data (if available),
     * and starts the background sync timer.
     */
    public static void initialize() {
        initializeSchema();

        // If MySQL is reachable now, warm up the SQLite cache.
        Connection mysql = DatabaseConnection.getMySQLConnection();
        if (mysql != null) {
            try {
                mirrorFromMySQL(mysql);
                System.out.println("[SqliteManager] Mirrored MySQL data to SQLite cache.");
            } catch (SQLException e) {
                System.err.println("[SqliteManager] Mirror failed: " + e.getMessage());
            } finally {
                close(mysql);
            }
        } else {
            System.out.println("[SqliteManager] MySQL unavailable at startup – using SQLite cache.");
        }

        startSyncTimer();
    }

    // ======================================================================
    // Schema creation
    // ======================================================================

    /** Creates all required tables in the SQLite database if they don't exist. */
    private static void initializeSchema() {
        try (Connection conn = DatabaseConnection.getSQLiteConnection();
             Statement stmt = conn.createStatement()) {

            // Enable WAL mode for better concurrent read/write performance.
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS flights (
                    flight_number TEXT PRIMARY KEY,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    departure_time TEXT NOT NULL,
                    arrival_time TEXT NOT NULL,
                    total_seats INTEGER NOT NULL,
                    available_seats INTEGER NOT NULL,
                    price REAL NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone TEXT,
                    passport_number TEXT DEFAULT '',
                    passenger_type TEXT DEFAULT 'Adult',
                    seat_preference TEXT DEFAULT 'Any',
                    travel_class TEXT DEFAULT 'Economy'
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    phone TEXT,
                    passenger_id TEXT,
                    FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE SET NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id TEXT PRIMARY KEY,
                    flight_number TEXT NOT NULL,
                    passenger_id TEXT NOT NULL,
                    seats INTEGER NOT NULL,
                    total_price REAL NOT NULL DEFAULT 0.0,
                    booking_date TEXT DEFAULT (datetime('now','localtime')),
                    booked_by_user_id TEXT,
                    payment_method TEXT DEFAULT 'Unknown',
                    FOREIGN KEY (flight_number) REFERENCES flights(flight_number) ON DELETE CASCADE,
                    FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE
                )
            """);

            System.out.println("[SqliteManager] SQLite schema ready.");
        } catch (SQLException e) {
            System.err.println("[SqliteManager] Schema init failed: " + e.getMessage());
        }
    }

    // ======================================================================
    // MySQL → SQLite  (mirror / warm cache)
    // ======================================================================

    /**
     * Copies all data from MySQL into the SQLite database.
     * Clears existing SQLite data first to ensure a clean mirror.
     */
    private static void mirrorFromMySQL(Connection mysql) throws SQLException {
        try (Connection sqlite = DatabaseConnection.getSQLiteConnection()) {
            sqlite.setAutoCommit(false);

            // Clear SQLite tables in reverse-dependency order.
            try (Statement stmt = sqlite.createStatement()) {
                stmt.execute("DELETE FROM reservations");
                stmt.execute("DELETE FROM users");
                stmt.execute("DELETE FROM passengers");
                stmt.execute("DELETE FROM flights");
            }

            // Copy flights
            copyTable(mysql, sqlite,
                "SELECT flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price FROM flights",
                "INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price) VALUES (?,?,?,?,?,?,?,?)",
                8);

            // Copy passengers
            copyTable(mysql, sqlite,
                "SELECT id, name, email, phone, passport_number, passenger_type, seat_preference, travel_class FROM passengers",
                "INSERT INTO passengers (id, name, email, phone, passport_number, passenger_type, seat_preference, travel_class) VALUES (?,?,?,?,?,?,?,?)",
                8);

            // Copy users
            copyTable(mysql, sqlite,
                "SELECT id, name, email, password, role, phone, passenger_id FROM users",
                "INSERT INTO users (id, name, email, password, role, phone, passenger_id) VALUES (?,?,?,?,?,?,?)",
                7);

            // Copy reservations
            copyTable(mysql, sqlite,
                "SELECT id, flight_number, passenger_id, seats, total_price, booking_date, booked_by_user_id, payment_method FROM reservations",
                "INSERT INTO reservations (id, flight_number, passenger_id, seats, total_price, booking_date, booked_by_user_id, payment_method) VALUES (?,?,?,?,?,?,?,?)",
                8);

            sqlite.commit();
        }
    }

    // ======================================================================
    // SQLite → MySQL  (sync on reconnect)
    // ======================================================================

    /**
     * Syncs all SQLite data back to MySQL.
     * Clears all MySQL tables then copies from SQLite, preserving dependency order.
     */
    private static void syncToMySQL(Connection mysql) throws SQLException {
        try (Connection sqlite = DatabaseConnection.getSQLiteConnection()) {
            mysql.setAutoCommit(false);

            // Clear MySQL tables in reverse-dependency order.
            try (Statement stmt = mysql.createStatement()) {
                stmt.execute("DELETE FROM reservations");
                stmt.execute("DELETE FROM users");
                stmt.execute("DELETE FROM passengers");
                stmt.execute("DELETE FROM flights");
            }

            // Copy tables in dependency order from SQLite → MySQL.
            copyTable(sqlite, mysql,
                "SELECT flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price FROM flights",
                "INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price) VALUES (?,?,?,?,?,?,?,?)",
                8);

            copyTable(sqlite, mysql,
                "SELECT id, name, email, phone, passport_number, passenger_type, seat_preference, travel_class FROM passengers",
                "INSERT INTO passengers (id, name, email, phone, passport_number, passenger_type, seat_preference, travel_class) VALUES (?,?,?,?,?,?,?,?)",
                8);

            copyTable(sqlite, mysql,
                "SELECT id, name, email, password, role, phone, passenger_id FROM users",
                "INSERT INTO users (id, name, email, password, role, phone, passenger_id) VALUES (?,?,?,?,?,?,?)",
                7);

            copyTable(sqlite, mysql,
                "SELECT id, flight_number, passenger_id, seats, total_price, booking_date, booked_by_user_id, payment_method FROM reservations",
                "INSERT INTO reservations (id, flight_number, passenger_id, seats, total_price, booking_date, booked_by_user_id, payment_method) VALUES (?,?,?,?,?,?,?,?)",
                8);

            mysql.commit();
            System.out.println("[SqliteManager] Successfully synced SQLite data back to MySQL.");
        }
    }

    // ======================================================================
    // Background sync timer
    // ======================================================================

    /** Starts a repeating timer that checks MySQL availability and syncs when needed. */
    private static void startSyncTimer() {
        syncTimer = new Timer("SQLiteSync", true); // daemon thread
        syncTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!DatabaseConnection.isOnline()) {
                        // We're in SQLite mode – try to reconnect to MySQL.
                        Connection mysql = DatabaseConnection.getMySQLConnection();
                        if (mysql != null) {
                            try {
                                System.out.println("[SqliteManager] MySQL reconnected – syncing offline changes...");
                                syncToMySQL(mysql);
                                System.out.println("[SqliteManager] Sync complete – switching back to MySQL.");
                                notifyModeChange(DatabaseConnection.Mode.MYSQL);
                            } catch (SQLException e) {
                                System.err.println("[SqliteManager] Sync to MySQL failed: " + e.getMessage());
                            } finally {
                                close(mysql);
                            }
                        }
                    } else {
                        // We're online – keep SQLite cache warm.
                        Connection mysql = DatabaseConnection.getMySQLConnection();
                        if (mysql != null) {
                            try {
                                mirrorFromMySQL(mysql);
                            } catch (SQLException e) {
                                System.err.println("[SqliteManager] Mirror failed: " + e.getMessage());
                            } finally {
                                close(mysql);
                            }
                        } else {
                            // MySQL just went down.
                            System.out.println("[SqliteManager] MySQL went offline – SQLite fallback active.");
                            notifyModeChange(DatabaseConnection.Mode.SQLITE);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[SqliteManager] Sync timer error: " + e.getMessage());
                }
            }
        }, SYNC_INTERVAL_MS, SYNC_INTERVAL_MS);
        System.out.println("[SqliteManager] Background sync timer started (interval: " + SYNC_INTERVAL_MS / 1000 + "s).");
    }

    /** Stops the background sync timer. */
    public static void shutdown() {
        if (syncTimer != null) {
            syncTimer.cancel();
            syncTimer = null;
            System.out.println("[SqliteManager] Sync timer stopped.");
        }
    }

    // ======================================================================
    // Helpers
    // ======================================================================

    /**
     * Generic row copier: reads rows from source using {@code selectSql}
     * and inserts them into dest using {@code insertSql}.
     */
    private static void copyTable(Connection source, Connection dest,
                                   String selectSql, String insertSql, int columnCount) throws SQLException {
        try (PreparedStatement read = source.prepareStatement(selectSql);
             ResultSet rs = read.executeQuery();
             PreparedStatement write = dest.prepareStatement(insertSql)) {
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    write.setObject(i, rs.getObject(i));
                }
                write.addBatch();
            }
            write.executeBatch();
        }
    }

    private static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { }
        }
    }

    private static void notifyModeChange(DatabaseConnection.Mode newMode) {
        ModeChangeListener listener = modeChangeListener;
        if (listener != null) {
            listener.onModeChanged(newMode);
        }
    }

    private SqliteManager() {
        // Prevent instantiation
    }
}
