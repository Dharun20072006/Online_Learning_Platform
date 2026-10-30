package onlinelearning.user;

/**
 * Base class representing a user in the Online Learning Platform.
 * Demonstrates:
 * - Encapsulation (private fields with public getters/setters)
 * - Abstraction (abstract method getRole())
 * - Base class for Inheritance (Student, Instructor, Admin)
 */
public abstract class User {
    private final int id;
    private String name;
    private final String email;

    /**
     * Constructs a new User.
     *
     * @param id    Unique identifier for the user
     * @param name  Full name of the user
     * @param email Email address of the user
     */
    public User(int id, String name, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.id = id;
        this.name = name.trim();
        this.email = email.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    /**
     * Abstract method defining the role of the user.
     * Each subclass must override this method.
     *
     * @return User role as a string (STUDENT, INSTRUCTOR, ADMIN)
     */
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("[%s] ID: %d | Name: %s | Email: %s", getRole(), id, name, email);
    }
}
