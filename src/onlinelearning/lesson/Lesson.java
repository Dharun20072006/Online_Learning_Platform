package onlinelearning.lesson;

/**
 * Abstract base class representing a lesson in a course.
 * Demonstrates:
 * - Encapsulation (private fields with public getters)
 * - Abstraction (abstract method isCompleted)
 * - Polymorphism (subclasses define their own completion logic)
 */
public abstract class Lesson {
    private final int id;
    private String title;
    private final int durationMinutes;
    private final int order;

    /**
     * Constructs a new Lesson.
     *
     * @param id              Unique lesson identifier
     * @param title           Title of the lesson
     * @param durationMinutes Estimated duration in minutes
     * @param order           Sequence order within the course
     */
    public Lesson(int id, String title, int durationMinutes, int order) {
        if (id <= 0) {
            throw new IllegalArgumentException("Lesson ID must be positive.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero.");
        }
        if (order < 0) {
            throw new IllegalArgumentException("Lesson order cannot be negative.");
        }
        this.id = id;
        this.title = title.trim();
        this.durationMinutes = durationMinutes;
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        }
        this.title = title.trim();
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Abstract method that determines if the lesson is considered complete.
     * Each lesson subclass implements its own completion rules:
     * - VideoLesson: watched >= 90% duration
     * - ReadingLesson: completion acknowledged
     * - QuizLesson: score >= 60%
     *
     * @param watchedMinutes Minutes watched (used by VideoLesson)
     * @param quizScore      Score obtained as percentage (used by QuizLesson)
     * @return true if completion conditions are satisfied
     */
    public abstract boolean isCompleted(int watchedMinutes, double quizScore);

    /**
     * Returns the human-readable type of the lesson.
     *
     * @return Lesson type (VIDEO, READING, QUIZ)
     */
    public abstract String getLessonType();

    @Override
    public String toString() {
        return String.format("[%s] #%d %s (%d mins)", getLessonType(), order, title, durationMinutes);
    }
}
