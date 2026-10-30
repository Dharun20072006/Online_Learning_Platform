package onlinelearning.user;

/**
 * Represents an Administrator in the Online Learning Platform.
 * Demonstrates Inheritance by extending User and Polymorphism by overriding getRole().
 */
public class Admin extends User {

    public Admin(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
