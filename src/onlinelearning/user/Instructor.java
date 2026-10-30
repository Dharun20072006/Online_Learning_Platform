package onlinelearning.user;

/**
 * Represents an Instructor in the Online Learning Platform.
 * Demonstrates Inheritance by extending User and Polymorphism by overriding getRole().
 */
public class Instructor extends User {

    public Instructor(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "INSTRUCTOR";
    }
}
