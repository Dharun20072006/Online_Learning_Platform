package onlinelearning.user;

/**
 * Represents a Student in the Online Learning Platform.
 * Demonstrates Inheritance by extending User and Polymorphism by overriding getRole().
 */
public class Student extends User {

    public Student(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }
}
