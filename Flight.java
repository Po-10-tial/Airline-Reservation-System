public class Flight {
    private final String flightNumber;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private double price;

    public Flight(String flightNumber, String origin, String destination, String departureTime, String arrivalTime, int totalSeats, double price) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.price = price;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public boolean reserveSeats(int count) {
        if (count <= 0 || count > availableSeats) {
            return false;
        }
        availableSeats -= count;
        return true;
    }

    public void releaseSeats(int count) {
        if (count > 0) {
            availableSeats += count;
            if (availableSeats > totalSeats) {
                availableSeats = totalSeats;
            }
        }
    }

    public int getReservedSeats() {
        return totalSeats - availableSeats;
    }

    public void updateDetails(String origin, String destination, String departureTime, String arrivalTime, int totalSeats, double price) {
        int reserved = getReservedSeats();
        if (totalSeats < reserved) {
            throw new IllegalArgumentException("Total seats cannot be less than already reserved seats.");
        }
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.price = price;
        this.availableSeats = totalSeats - reserved;
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s, Departs %s, Arrives %s, Seats %d/%d, KSh %.2f",
                flightNumber, origin, destination, departureTime, arrivalTime, availableSeats, totalSeats, price);
    }
}
