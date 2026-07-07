# Airline Reservation System

A Java-based airline reservation system with a Swing user interface, backed by **MySQL** for persistent data storage and an embedded **SQLite** database as an automatic failsafe.

## Features

- **Flight Management** — List, search, add, edit, and delete flights (admin)
- **Booking System** — Book flights with passenger details, travel class, and payment method
- **Reservation Management** — View and cancel reservations with authorization checks
- **Admin Dashboard** — Real-time metrics for flights, customers, reservations, and seat availability
- **Customer Dashboard** — Browse flights, book tickets, and manage personal reservations
- **Role-Based Access** — Separate admin and customer login with registration
- **Database Failover** — Automatic MySQL → SQLite fallback with live sync on reconnection
- **Connection Status** — 🟠 Offline indicator appears in the status bar when SQLite is active

## Files

| File | Description |
|------|-------------|
| `AirlineReservationApp.java` | Main application entry point |
| `AirlineReservationUI.java` | Swing user interface for all views |
| `AirlineService.java` | Business logic layer (JDBC database operations) |
| `DatabaseConfig.java` | MySQL and SQLite connection settings |
| `DatabaseConnection.java` | Dual-mode JDBC connection manager (MySQL/SQLite failover) |
| `SqliteManager.java` | SQLite schema init, data mirroring, sync, and background timer |
| `Flight.java` | Flight model with seat availability tracking |
| `Passenger.java` | Passenger data record |
| `Reservation.java` | Reservation record with booking metadata |
| `User.java` | User account record |
| `Role.java` | Role enum (`ADMIN`, `CUSTOMER`) |
| `BookingPassenger.java` | Booking passenger data record |
| `airline_reservation_db.sql` | MySQL database schema and seed data |

## Prerequisites

1. **Java JDK 17+** (tested with JDK 23)
2. **MySQL Server** (8.0 or later) installed and running
3. **MySQL Connector/J** — `mysql-connector-j-9.7.0.jar` (included in project root)
4. **SQLite JDBC** — `sqlite-jdbc-3.46.1.3.jar` (included in project root)

## Database Setup

1. Start your MySQL server.
2. Run the schema file to create the database, tables, and seed data:

```powershell
mysql -u root -p < airline_reservation_db.sql
```

3. Update `DatabaseConfig.java` if your MySQL credentials differ from the defaults:
   - URL: `jdbc:mysql://localhost:3306/airline_reservation_db`
   - User: `root`
   - Password: (set in the file)

> **Note:** The SQLite database (`airline_backup.db`) is created automatically on first run — no setup required.

## Compile

```powershell
javac -cp ".;mysql-connector-j-9.7.0.jar;sqlite-jdbc-3.46.1.3.jar" *.java
```

## Run

### Swing UI (default)

```powershell
java -cp ".;mysql-connector-j-9.7.0.jar;sqlite-jdbc-3.46.1.3.jar" AirlineReservationApp
```

### Console Mode

```powershell
java -cp ".;mysql-connector-j-9.7.0.jar;sqlite-jdbc-3.46.1.3.jar" AirlineReservationApp console
```

## Default Admin Login

- **Email:** `admin@system.com`
- **Password:** `admin123`

## SQLite Failover

The application includes an embedded SQLite database that acts as an automatic failsafe:

```
App starts → Try MySQL
  ├── ✅ MySQL OK → Use MySQL, mirror data to SQLite (warm cache)
  └── ❌ MySQL down → Use SQLite (offline mode)
       └── Background timer checks MySQL every 30s
            └── MySQL reconnects → Sync SQLite → MySQL → Switch back
```

- **Transparent:** `AirlineService.java` is unaware of the failover — it simply calls `DatabaseConnection.getConnection()` and gets the right connection.
- **Automatic sync:** When MySQL comes back online, all changes made while offline are synced back automatically.
- **Status indicator:** The bottom-right corner of the UI shows an 🟠 **Offline (SQLite)** indicator when the local fallback is active.

## Technical Notes

- Data is **persisted in MySQL** and mirrored to SQLite — it survives application restarts and database outages.
- All database operations use **PreparedStatements** to prevent SQL injection.
- Multi-step operations (booking, cancellation) use **transactions** for data consistency.
- The SQLite fallback runs on a **daemon thread** so it won't prevent the application from exiting.
