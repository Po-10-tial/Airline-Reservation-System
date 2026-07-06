import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class AirlineReservationApp {
    private final AirlineService service = new AirlineService();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            new AirlineReservationApp().run();
        } else {
            SwingUtilities.invokeLater(() -> new AirlineReservationUI().show());
        }
    }

    private void run() {
        System.out.println("Welcome to the Airline Reservation System");
        boolean running = true;
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listAllFlights();
                case "2" -> searchFlights();
                case "3" -> bookFlight();
                case "4" -> cancelReservation();
                case "5" -> listReservations();
                case "6" -> {
                    running = false;
                    System.out.println("Exiting. Thank you for using the system.");
                }
                default -> System.out.println("Invalid option. Please enter a number between 1 and 6.");
            }
            System.out.println();
        }
    }

    private void showMenu() {
        System.out.println("\nPlease choose an option:");
        System.out.println("1. List all flights");
        System.out.println("2. Search flights by route");
        System.out.println("3. Book a flight");
        System.out.println("4. Cancel a reservation");
        System.out.println("5. View reservations");
        System.out.println("6. Exit");
        System.out.print("Choice: ");
    }

    private void listAllFlights() {
        List<Flight> flights = service.getAllFlights();
        if (flights.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }
        System.out.println("Available flights:");
        flights.forEach(f -> System.out.println(" - " + f));
    }

    private void searchFlights() {
        System.out.print("Origin: ");
        String origin = scanner.nextLine();
        System.out.print("Destination: ");
        String destination = scanner.nextLine();
        List<Flight> matches = service.searchFlights(origin, destination);
        if (matches.isEmpty()) {
            System.out.println("No flights found for that route.");
            return;
        }
        System.out.println("Matching flights:");
        matches.forEach(f -> System.out.println(" - " + f));
    }

    private void bookFlight() {
        try {
            System.out.print("Flight number: ");
            String flightNumber = scanner.nextLine();
            System.out.print("Passenger name: ");
            String name = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Number of seats: ");
            int seats = Integer.parseInt(scanner.nextLine().trim());

            Reservation reservation = service.bookFlight(flightNumber, name, email, phone, seats);
            System.out.println("Booking successful! Details:");
            System.out.println(reservation);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number of seats.");
        } catch (IllegalArgumentException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private void cancelReservation() {
        System.out.print("Reservation ID to cancel: ");
        String reservationId = scanner.nextLine();
        boolean cancelled = service.cancelReservation(reservationId);
        if (cancelled) {
            System.out.println("Reservation " + reservationId + " cancelled successfully.");
        } else {
            System.out.println("Reservation not found: " + reservationId);
        }
    }

    private void listReservations() {
        List<Reservation> reservations = service.getAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        System.out.println("Reservations:");
        reservations.forEach(r -> System.out.println(" - " + r));
    }
}
