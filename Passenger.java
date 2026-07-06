public record Passenger(String id, String name, String email, String phone, String passportNumber, String passengerType, String seatPreference, String travelClass) {
    public static Passenger create(String id, String name, String email, String phone, String passportNumber, String passengerType, String seatPreference, String travelClass) {
        return new Passenger(id, name, email, phone, passportNumber, passengerType, seatPreference, travelClass);
    }

    @Override
    public String toString() {
        return String.format("Passenger[id=%s, name=%s, passport=%s, type=%s, seat=%s, class=%s]", id, name, passportNumber, passengerType, seatPreference, travelClass);
    }
}
