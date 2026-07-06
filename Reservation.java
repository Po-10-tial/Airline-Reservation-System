import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Reservation(String reservationId, Flight flight, Passenger passenger, int seatCount, double totalPrice, String bookingDate, String bookedByUserId, String paymentMethod) {
    public static Reservation create(String reservationId, Flight flight, Passenger passenger, int seatCount, double totalPrice, String bookedByUserId, String paymentMethod) {
        String bookingDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new Reservation(reservationId, flight, passenger, seatCount, totalPrice, bookingDate, bookedByUserId, paymentMethod);
    }

    @Override
    public String toString() {
        return String.format("Reservation[id=%s, flight=%s, passenger=%s, seats=%d, price=KSh %.2f, payment=%s, booked=%s]",
                reservationId, flight.getFlightNumber(), passenger.name(), seatCount, totalPrice, paymentMethod, bookingDate);
    }
}
