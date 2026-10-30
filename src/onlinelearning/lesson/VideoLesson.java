package onlinelearning.lesson;

/**
 * Represents a video-based lesson.
 * Business Rule: A Video lesson is marked complete only if the student has watched
 * at least 90% of its duration.
 */
public class VideoLesson extends Lesson {

    public VideoLesson(int id, String title, int durationMinutes, int order) {
        super(id, title, durationMinutes, order);
    }

    /**
     * Checks if the video lesson completion rule is satisfied.
     * The student must have watched at least 90% of the video duration.
     *
     * @param watchedMinutes Number of minutes the student watched
     * @param quizScore      Ignored for video lessons
     * @return true if watched >= 90% of duration
     */
    @Override
    public boolean isCompleted(int watchedMinutes, double quizScore) {
        int requiredMinutes = (int) Math.ceil(getDurationMinutes() * 0.90);
        return watchedMinutes >= requiredMinutes;
    }

    @Override
    public String getLessonType() {
        return "VIDEO";
    }
}
