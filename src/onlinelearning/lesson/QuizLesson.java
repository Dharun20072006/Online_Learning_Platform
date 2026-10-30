package onlinelearning.lesson;

/**
 * Represents a quiz assessment lesson.
 * Business Rules:
 * - A Quiz lesson is passed with a score of 60% or more.
 * - A student gets at most 3 attempts.
 * - After 3 failed attempts, the student must wait 24 hours.
 */
public class QuizLesson extends Lesson {
    public static final double PASSING_SCORE = 60.0;
    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCKOUT_HOURS = 24;

    public QuizLesson(int id, String title, int durationMinutes, int order) {
        super(id, title, durationMinutes, order);
    }

    /**
     * Evaluates whether the quiz attempt satisfies the passing requirement.
     *
     * @param watchedMinutes Ignored for quiz lessons
     * @param quizScore      Score achieved by the student (in percentage, 0-100)
     * @return true if score is 60% or above
     */
    @Override
    public boolean isCompleted(int watchedMinutes, double quizScore) {
        return quizScore >= PASSING_SCORE;
    }

    @Override
    public String getLessonType() {
        return "QUIZ";
    }
}
