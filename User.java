public record User(String id, String name, String email, String password, Role role, String phone, String passengerId) {
    public User withPassengerId(String passengerId) {
        return new User(id, name, email, password, role, phone, passengerId);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, email=%s, role=%s]", id, name, email, role);
    }
}
