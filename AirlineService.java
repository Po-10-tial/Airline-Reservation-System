import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business-logic layer for the Airline Reservation System.
 * All data is persisted to a MySQL database via JDBC.
 */
public class AirlineService {

    private static final String ADMIN_SECRET = "SKYLINE-ADMIN";

    public AirlineService() {
        // No in-memory initialisation — data lives in the database.
    }

    // ======================================================================
    // Flight operations
    // ======================================================================

    public List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights ORDER BY flight_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                flights.add(mapFlight(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load flights: " + e.getMessage(), e);
        }
        return flights;
    }

    public List<Flight> searchFlights(String origin, String destination) {
        if (origin == null || destination == null) {
            return new ArrayList<>();
        }
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE LOWER(origin) = LOWER(?) AND LOWER(destination) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, origin.trim());
            ps.setString(2, destination.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flights.add(mapFlight(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search flights: " + e.getMessage(), e);
        }
        return flights;
    }

    public Flight getFlightByNumber(String flightNumber) {
        return findFlightByNumber(flightNumber);
    }

    public void addFlight(String flightNumber, String origin, String destination,
                          String departureTime, String arrivalTime, int totalSeats, double price) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("Flight number is required.");
        }
        if (findFlightByNumber(flightNumber) != null) {
            throw new IllegalArgumentException("A flight already exists with this number.");
        }
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than zero.");
        }
        String sql = "INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber.trim().toUpperCase());
            ps.setString(2, origin.trim());
            ps.setString(3, destination.trim());
            ps.setString(4, departureTime.trim());
            ps.setString(5, arrivalTime.trim());
            ps.setInt(6, totalSeats);
            ps.setInt(7, totalSeats); // available = total on creation
            ps.setDouble(8, price);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add flight: " + e.getMessage(), e);
        }
    }

    public void updateFlight(String flightNumber, String origin, String destination,
                             String departureTime, String arrivalTime, int totalSeats, double price) {
        Flight existing = findFlightByNumber(flightNumber);
        if (existing == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
        int reserved = existing.getReservedSeats();
        if (totalSeats < reserved) {
            throw new IllegalArgumentException("Total seats cannot be less than already reserved seats.");
        }
        int newAvailable = totalSeats - reserved;
        String sql = "UPDATE flights SET origin=?, destination=?, departure_time=?, arrival_time=?, total_seats=?, available_seats=?, price=? WHERE flight_number=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, origin.trim());
            ps.setString(2, destination.trim());
            ps.setString(3, departureTime.trim());
            ps.setString(4, arrivalTime.trim());
            ps.setInt(5, totalSeats);
            ps.setInt(6, newAvailable);
            ps.setDouble(7, price);
            ps.setString(8, flightNumber.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update flight: " + e.getMessage(), e);
        }
    }

    public boolean deleteFlight(String flightNumber) {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        // Check for existing reservations
        String countSql = "SELECT COUNT(*) FROM reservations WHERE flight_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, flightNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Cannot delete a flight with existing reservations.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check reservations: " + e.getMessage(), e);
        }
        String sql = "DELETE FROM flights WHERE flight_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete flight: " + e.getMessage(), e);
        }
    }

    // ======================================================================
    // Booking operations
    // ======================================================================

    /** Console / simple booking (no user account required). */
    public Reservation bookFlight(String flightNumber, String passengerName, String email, String phone, int seats) {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
        if (seats <= 0) {
            throw new IllegalArgumentException("Seat count must be at least 1.");
        }
        if (flight.getAvailableSeats() < seats) {
            throw new IllegalArgumentException("Not enough seats available.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Create passenger
            String passengerId = generateId("P");
            insertPassenger(conn, passengerId, passengerName, email, phone, "", "Adult", "Any", "Economy");

            // Decrease available seats
            updateAvailableSeats(conn, flightNumber, -seats);

            // Create reservation
            String reservationId = generateId("R");
            double totalPrice = flight.getPrice() * seats;
            insertReservation(conn, reservationId, flightNumber, passengerId, seats, totalPrice, null, "Unknown");

            conn.commit();

            Passenger passenger = Passenger.create(passengerId, passengerName, email, phone, "", "Adult", "Any", "Economy");
            return Reservation.create(reservationId, flight, passenger, seats, totalPrice, null, "Unknown");
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /** GUI booking for a logged-in customer with passenger details. */
    public Reservation bookFlightForPassenger(String flightNumber, String userId,
                                              String passengerName, String email, String phone,
                                              String passportNumber, String passengerType,
                                              String seatPreference, String travelClass, String paymentMethod) {
        User user = getUserById(userId);
        if (user == null || !user.isCustomer()) {
            throw new IllegalArgumentException("Invalid customer account.");
        }
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
        if (flight.getAvailableSeats() < 1) {
            throw new IllegalArgumentException("Not enough seats available for this flight.");
        }

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedPhone = phone == null ? "" : phone.trim();
        String normalizedPassport = passportNumber == null ? "" : passportNumber.trim();
        String normalizedPassengerType = passengerType == null || passengerType.isBlank() ? "Adult" : passengerType.trim();
        String normalizedSeatPreference = seatPreference == null || seatPreference.isBlank() ? "Aisle" : seatPreference.trim();
        String normalizedTravelClass = travelClass == null || travelClass.isBlank() ? "Economy" : travelClass.trim();
        String normalizedPaymentMethod = paymentMethod == null || paymentMethod.isBlank() ? "Mpesa" : paymentMethod.trim();

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String passengerId = generateId("P");
            insertPassenger(conn, passengerId, passengerName.trim(), normalizedEmail, normalizedPhone,
                    normalizedPassport, normalizedPassengerType, normalizedSeatPreference, normalizedTravelClass);

            updateAvailableSeats(conn, flightNumber, -1);

            double classMultiplier = travelClassMultiplier(normalizedTravelClass);
            double totalPrice = flight.getPrice() * classMultiplier;
            String reservationId = generateId("R");
            insertReservation(conn, reservationId, flightNumber, passengerId, 1, totalPrice, userId, normalizedPaymentMethod);

            conn.commit();

            Passenger passenger = Passenger.create(passengerId, passengerName.trim(), normalizedEmail, normalizedPhone,
                    normalizedPassport, normalizedPassengerType, normalizedSeatPreference, normalizedTravelClass);
            return Reservation.create(reservationId, flight, passenger, 1, totalPrice, userId, normalizedPaymentMethod);
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /** GUI booking for a logged-in customer using their profile. */
    public Reservation bookFlightForUser(String flightNumber, String userId, int seats) {
        User user = getUserById(userId);
        if (user == null || !user.isCustomer()) {
            throw new IllegalArgumentException("Invalid customer account.");
        }
        if (user.passengerId() == null) {
            throw new IllegalStateException("Customer does not have an associated passenger profile.");
        }
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
        if (seats <= 0) {
            throw new IllegalArgumentException("Seat count must be at least 1.");
        }
        if (flight.getAvailableSeats() < seats) {
            throw new IllegalArgumentException("Not enough seats available.");
        }
        Passenger passenger = findPassengerById(user.passengerId());
        if (passenger == null) {
            throw new IllegalStateException("Passenger profile missing for user.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            updateAvailableSeats(conn, flightNumber, -seats);

            String reservationId = generateId("R");
            double totalPrice = flight.getPrice() * seats;
            insertReservation(conn, reservationId, flightNumber, passenger.id(), seats, totalPrice, userId, "Unknown");

            conn.commit();

            return Reservation.create(reservationId, flight, passenger, seats, totalPrice, userId, "Unknown");
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    // ======================================================================
    // Cancellation
    // ======================================================================

    /** Console cancellation (no user check). */
    public boolean cancelReservation(String reservationId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Load the reservation first to know how many seats to release
            String findSql = "SELECT flight_number, seats FROM reservations WHERE id = ?";
            String flightNumber;
            int seats;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, reservationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    flightNumber = rs.getString("flight_number");
                    seats = rs.getInt("seats");
                }
            }

            // Delete the reservation
            String deleteSql = "DELETE FROM reservations WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, reservationId);
                ps.executeUpdate();
            }

            // Release seats
            updateAvailableSeats(conn, flightNumber, seats);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Cancellation failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /** GUI cancellation with user authorization. */
    public boolean cancelReservation(String reservationId, String userId) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Load reservation details
            String findSql = "SELECT flight_number, passenger_id, seats, booked_by_user_id FROM reservations WHERE id = ?";
            String flightNumber;
            String passengerId;
            int seats;
            String bookedByUserId;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, reservationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    flightNumber = rs.getString("flight_number");
                    passengerId = rs.getString("passenger_id");
                    seats = rs.getInt("seats");
                    bookedByUserId = rs.getString("booked_by_user_id");
                }
            }

            // Authorization check
            boolean allowed = user.isAdmin()
                    || (user.isCustomer() && bookedByUserId != null && bookedByUserId.equals(userId))
                    || (user.isCustomer() && user.passengerId() != null && user.passengerId().equals(passengerId));
            if (!allowed) {
                conn.rollback();
                return false;
            }

            // Delete and release seats
            String deleteSql = "DELETE FROM reservations WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, reservationId);
                ps.executeUpdate();
            }
            updateAvailableSeats(conn, flightNumber, seats);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Cancellation failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    // ======================================================================
    // Reservation queries
    // ======================================================================

    public List<Reservation> getAllReservations() {
        return queryReservations("SELECT r.id, r.flight_number, r.passenger_id, r.seats, r.total_price, r.booking_date, r.booked_by_user_id, r.payment_method FROM reservations r ORDER BY r.booking_date DESC", null);
    }

    public List<Reservation> getReservationsForUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        if (user.isAdmin()) {
            return getAllReservations();
        }
        String sql = "SELECT r.id, r.flight_number, r.passenger_id, r.seats, r.total_price, r.booking_date, r.booked_by_user_id, r.payment_method " +
                     "FROM reservations r WHERE r.booked_by_user_id = ? " +
                     "OR r.passenger_id = ? ORDER BY r.booking_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.id());
            ps.setString(2, user.passengerId() != null ? user.passengerId() : "");
            try (ResultSet rs = ps.executeQuery()) {
                return buildReservationList(conn, rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load reservations: " + e.getMessage(), e);
        }
    }

    public List<Reservation> getReservationsByFlight(String flightNumber) {
        String sql = "SELECT r.id, r.flight_number, r.passenger_id, r.seats, r.total_price, r.booking_date, r.booked_by_user_id, r.payment_method " +
                     "FROM reservations r WHERE r.flight_number = ? ORDER BY r.booking_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return buildReservationList(conn, rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load reservations: " + e.getMessage(), e);
        }
    }

    // ======================================================================
    // User / Auth operations
    // ======================================================================

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Invalid login credentials.");
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Email or password is incorrect.");
                }
                User user = mapUser(rs);
                if (!user.password().equals(password)) {
                    throw new IllegalArgumentException("Email or password is incorrect.");
                }
                return user;
            }
        } catch (IllegalArgumentException e) {
            throw e; // re-throw business exceptions
        } catch (SQLException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    public User registerCustomer(String name, String email, String password, String phone) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Name, email, and password are required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (emailExists(normalizedEmail)) {
            throw new IllegalArgumentException("An account already exists with that email.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Create passenger profile
            String passengerId = generateId("P");
            insertPassenger(conn, passengerId, name.trim(), normalizedEmail,
                    phone == null ? "" : phone.trim(), "", "Adult", "Any", "Economy");

            // Create user
            String userId = generateId("U");
            insertUser(conn, userId, name.trim(), normalizedEmail, password, "CUSTOMER",
                    phone == null ? "" : phone.trim(), passengerId);

            conn.commit();
            return new User(userId, name.trim(), normalizedEmail, password, Role.CUSTOMER,
                    phone == null ? "" : phone.trim(), passengerId);
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    public User registerAdmin(String name, String email, String password, String phone, String adminCode) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank() || adminCode == null || adminCode.isBlank()) {
            throw new IllegalArgumentException("Name, email, password, and admin code are required.");
        }
        if (!ADMIN_SECRET.equals(adminCode.trim())) {
            throw new IllegalArgumentException("Invalid admin code.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (emailExists(normalizedEmail)) {
            throw new IllegalArgumentException("An account already exists with that email.");
        }

        String userId = generateId("U");
        String sql = "INSERT INTO users (id, name, email, password, role, phone, passenger_id) VALUES (?,?,?,?,?,?,NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name.trim());
            ps.setString(3, normalizedEmail);
            ps.setString(4, password);
            ps.setString(5, "ADMIN");
            ps.setString(6, phone == null ? "" : phone.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Admin registration failed: " + e.getMessage(), e);
        }
        return new User(userId, name.trim(), normalizedEmail, password, Role.ADMIN,
                phone == null ? "" : phone.trim(), null);
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Email check failed: " + e.getMessage(), e);
        }
    }

    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user: " + e.getMessage(), e);
        }
    }

    public List<User> getAllCustomers() {
        List<User> customers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load customers: " + e.getMessage(), e);
        }
        return customers;
    }

    public List<User> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return getAllCustomers();
        }
        String likePattern = "%" + query.trim().toLowerCase() + "%";
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?) ORDER BY name";
        List<User> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Customer search failed: " + e.getMessage(), e);
        }
        return customers;
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passengers ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPassenger(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load passengers: " + e.getMessage(), e);
        }
        return list;
    }

    // ======================================================================
    // Statistics
    // ======================================================================

    public int getTotalFlightCount() {
        return countQuery("SELECT COUNT(*) FROM flights");
    }

    public int getTotalReservationCount() {
        return countQuery("SELECT COUNT(*) FROM reservations");
    }

    public int getTotalCustomerCount() {
        return countQuery("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'");
    }

    public int getAvailableSeatCount() {
        String sql = "SELECT COALESCE(SUM(available_seats), 0) FROM flights";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count seats: " + e.getMessage(), e);
        }
    }

    // ======================================================================
    // Private helpers — mapping & SQL utilities
    // ======================================================================

    private Flight findFlightByNumber(String flightNumber) {
        if (flightNumber == null) return null;
        String sql = "SELECT * FROM flights WHERE flight_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFlight(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Flight lookup failed: " + e.getMessage(), e);
        }
    }

    private Passenger findPassengerById(String passengerId) {
        if (passengerId == null) return null;
        String sql = "SELECT * FROM passengers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPassenger(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Passenger lookup failed: " + e.getMessage(), e);
        }
    }

    private double travelClassMultiplier(String travelClass) {
        return switch (travelClass.toLowerCase()) {
            case "business" -> 1.5;
            case "first", "first class" -> 2.0;
            default -> 1.0;
        };
    }

    /** Run a simple SELECT COUNT(*) query and return the result. */
    private int countQuery(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Count query failed: " + e.getMessage(), e);
        }
    }

    // ----- INSERT helpers (use a provided Connection for transaction support) -----

    private void insertPassenger(Connection conn, String id, String name, String email,
                                 String phone, String passport, String type, String seat, String travelClass) throws SQLException {
        String sql = "INSERT INTO passengers (id, name, email, phone, passport_number, passenger_type, seat_preference, travel_class) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, passport);
            ps.setString(6, type);
            ps.setString(7, seat);
            ps.setString(8, travelClass);
            ps.executeUpdate();
        }
    }

    private void insertUser(Connection conn, String id, String name, String email, String password,
                            String role, String phone, String passengerId) throws SQLException {
        String sql = "INSERT INTO users (id, name, email, password, role, phone, passenger_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, role);
            ps.setString(6, phone);
            ps.setString(7, passengerId);
            ps.executeUpdate();
        }
    }

    private void insertReservation(Connection conn, String id, String flightNumber, String passengerId,
                                   int seats, double totalPrice, String bookedByUserId, String paymentMethod) throws SQLException {
        String sql = "INSERT INTO reservations (id, flight_number, passenger_id, seats, total_price, booked_by_user_id, payment_method) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, flightNumber);
            ps.setString(3, passengerId);
            ps.setInt(4, seats);
            ps.setDouble(5, totalPrice);
            ps.setString(6, bookedByUserId);
            ps.setString(7, paymentMethod);
            ps.executeUpdate();
        }
    }

    private void updateAvailableSeats(Connection conn, String flightNumber, int delta) throws SQLException {
        String sql = "UPDATE flights SET available_seats = available_seats + ? WHERE flight_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, flightNumber.trim());
            ps.executeUpdate();
        }
    }

    // ----- ResultSet -> Object mappers -----

    private Flight mapFlight(ResultSet rs) throws SQLException {
        Flight f = new Flight(
                rs.getString("flight_number"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getString("departure_time"),
                rs.getString("arrival_time"),
                rs.getInt("total_seats"),
                rs.getDouble("price")
        );
        // The constructor sets available = total; override with the actual DB value.
        int dbAvailable = rs.getInt("available_seats");
        int diff = f.getAvailableSeats() - dbAvailable;
        if (diff > 0) {
            f.reserveSeats(diff);
        }
        return f;
    }

    private Passenger mapPassenger(ResultSet rs) throws SQLException {
        return Passenger.create(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("passport_number"),
                rs.getString("passenger_type"),
                rs.getString("seat_preference"),
                rs.getString("travel_class")
        );
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role")),
                rs.getString("phone"),
                rs.getString("passenger_id")
        );
    }

    // ----- Reservation query helpers -----

    private List<Reservation> queryReservations(String sql, String[] params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setString(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                return buildReservationList(conn, rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Reservation query failed: " + e.getMessage(), e);
        }
    }

    private List<Reservation> buildReservationList(Connection conn, ResultSet rs) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        while (rs.next()) {
            String resId = rs.getString("id");
            String flightNum = rs.getString("flight_number");
            String passId = rs.getString("passenger_id");
            int seats = rs.getInt("seats");
            double totalPrice = rs.getDouble("total_price");
            Timestamp bookingTs = rs.getTimestamp("booking_date");
            String bookedBy = rs.getString("booked_by_user_id");
            String payment = rs.getString("payment_method");

            Flight flight = findFlightByNumber(flightNum);
            Passenger passenger = findPassengerById(passId);

            if (flight != null && passenger != null) {
                String bookingDate = bookingTs != null ? bookingTs.toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                list.add(new Reservation(resId, flight, passenger, seats, totalPrice, bookingDate, bookedBy, payment));
            }
        }
        return list;
    }

    // ----- ID generation -----

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ----- Connection utilities -----

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
