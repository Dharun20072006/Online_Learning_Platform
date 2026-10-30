package onlinelearning.course;

import onlinelearning.lesson.Lesson;
import onlinelearning.user.Instructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a Course in the Online Learning Platform.
 * Demonstrates Encapsulation and business rule validations.
 *
 * Business Rules:
 * - A course must have a title, category, level, and price (>= 0; 0 means free).
 * - A course can be published only if it has at least 1 section and at least 3 lessons.
 * - Once published, category and level CANNOT be changed.
 * - Title and description can be edited after publishing.
 * - Price changes apply only to new enrollments.
 * - A course with average rating < 2.5 after >= 20 ratings is flagged for admin review.
 */
public class Course {
    private final int id;
    private String title;
    private String description;
    private final Category category; // Immutable once created
    private final Level level;       // Immutable once created
    private double price;            // Price >= 0; 0 is free
    private final Instructor instructor;
    private CourseStatus status;
    private final List<Section> sections;
    private double averageRating;
    private int ratingCount;
    private boolean flaggedForReview;

    public Course(int id, String title, String description, Category category,
                  Level level, double price, Instructor instructor) {
        if (id <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category must be specified.");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level must be specified.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative. Enter 0 for free course.");
        }
        if (instructor == null) {
            throw new IllegalArgumentException("Course must have an assigned instructor.");
        }

        this.id = id;
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.category = category;
        this.level = level;
        this.price = price;
        this.instructor = instructor;
        this.status = CourseStatus.DRAFT;
        this.sections = new ArrayList<>();
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.flaggedForReview = false;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Level getLevel() {
        return level;
    }

    public double getPrice() {
        return price;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public boolean isFree() {
        return this.price == 0.0;
    }

    /**
     * Total number of lessons across all sections in the course.
     */
    public int getTotalLessonCount() {
        return sections.stream().mapToInt(Section::getLessonCount).sum();
    }

    /**
     * Returns a flat list of all lessons in this course sorted in order.
     */
    public List<Lesson> getAllLessonsOrdered() {
        List<Lesson> all = new ArrayList<>();
        for (Section section : sections) {
            all.addAll(section.getLessons());
        }
        all.sort(Comparator.comparingInt(Lesson::getOrder));
        return all;
    }

    /**
     * Adds a section to this course.
     */
    public void addSection(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null.");
        }
        sections.add(section);
    }

    /**
     * Publishes the course.
     * Business Rule: A course can be published only if it has at least one section
     * and at least 3 lessons across the course.
     */
    public void publish() {
        if (sections.isEmpty()) {
            throw new IllegalStateException("Cannot publish course: Must have at least one section.");
        }
        int totalLessons = getTotalLessonCount();
        if (totalLessons < 3) {
            throw new IllegalStateException(String.format(
                    "Cannot publish course: Must have at least 3 lessons across sections (currently has %d).", totalLessons));
        }
        this.status = CourseStatus.PUBLISHED;
    }

    /**
     * Edits the course title and description.
     * Business Rule: Once a course is published, title and description can still be edited.
     */
    public void edit(String newTitle, String newDescription) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        this.title = newTitle.trim();
        if (newDescription != null) {
            this.description = newDescription.trim();
        }
    }

    /**
     * Updates the course price.
     * Business Rule: Price change applies only to new enrollments.
     */
    public void changePrice(double newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = newPrice;
    }

    /**
     * Withdraws the course (typically executed by Admin).
     */
    public void withdraw() {
        this.status = CourseStatus.WITHDRAWN;
    }

    /**
     * Recalculates and updates the course's average rating.
     * Business Rule: A course with an average rating below 2.5 after at least 20 ratings
     * is flagged for admin review.
     */
    public void updateRating(double newAverageRating, int newRatingCount) {
        this.averageRating = newAverageRating;
        this.ratingCount = newRatingCount;

        if (this.ratingCount >= 20 && this.averageRating < 2.5) {
            this.flaggedForReview = true;
        } else {
            this.flaggedForReview = false;
        }
    }

    @Override
    public String toString() {
        return String.format("Course #%d: \"%s\" | %s | %s | Price: ₹%.2f | Status: %s | Rating: %.1f (%d reviews) | Instructor: %s",
                id, title, category, level, price, status, averageRating, ratingCount, instructor.getName());
    }
}
