# Airline Reservation System

This workspace contains a Java-based airline reservation system with a Swing user interface, backed by a **MySQL database** for persistent data storage.

## Files

- `AirlineReservationApp.java` - main application entry point.
- `AirlineReservationUI.java` - Swing user interface for booking, searching, and managing reservations.
- `AirlineService.java` - business logic layer (JDBC database operations).
- `DatabaseConfig.java` - MySQL connection settings (URL, username, password).
- `DatabaseConnection.java` - JDBC connection manager.
- `Flight.java` - flight model with seat availability tracking.
- `Passenger.java` - passenger data record.
- `Reservation.java` - reservation record with booking metadata.
- `User.java` - user account record.
- `Role.java` - role enum (ADMIN, CUSTOMER).
- `BookingPassenger.java` - booking passenger data record.
- `airline_reservation_db.sql` - MySQL database schema and seed data.

## Prerequisites

1. **Java JDK 17+** (tested with JDK 23)
2. **MySQL Server** (8.0 or later) installed and running
3. **MySQL Connector/J** JAR file on the classpath

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

## Compile

```powershell
javac -cp ".;mysql-connector-j-9.7.0.jar" *.java
```

> **Note:** Replace `mysql-connector-j-9.7.0.jar` with the actual filename of your MySQL connector JAR.

## Run

### Swing UI (default)

```powershell
java -cp ".;mysql-connector-j-9.7.0.jar" AirlineReservationApp
```

### Console Mode

```powershell
java -cp ".;mysql-connector-j-9.7.0.jar" AirlineReservationApp console
```

## Default Admin Login

- **Email:** `admin@system.com`
- **Password:** `admin123`

## Usage

The application supports:

- Listing all flights
- Searching flights by origin and destination
- Booking a flight (with passenger details, travel class, payment method)
- Cancelling a reservation
- Viewing current reservations
- Admin dashboard with flight/customer/reservation management

## Notes

- Data is now **persisted in MySQL** — it survives application restarts.
- All database operations use **PreparedStatements** to prevent SQL injection.
- Multi-step operations (booking, cancellation) use **transactions** for data consistency.
