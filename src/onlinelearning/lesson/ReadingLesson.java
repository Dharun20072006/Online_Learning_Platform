package onlinelearning.lesson;

/**
 * Represents a text-based reading lesson.
 * Business Rule: A Reading lesson is marked complete when the student finishes reading
 * the material and confirms completion.
 */
public class ReadingLesson extends Lesson {

    public ReadingLesson(int id, String title, int durationMinutes, int order) {
        super(id, title, durationMinutes, order);
    }

    /**
     * Reading lessons are completed once the student acknowledges reading.
     *
     * @param watchedMinutes Ignored for reading lessons
     * @param quizScore      Ignored for reading lessons
     * @return true
     */
    @Override
    public boolean isCompleted(int watchedMinutes, double quizScore) {
        return true;
    }

    @Override
    public String getLessonType() {
        return "READING";
    }
}
