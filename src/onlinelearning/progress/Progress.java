package onlinelearning.progress;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks a student's learning progress within an enrolled course.
 * Encapsulates completed lessons, quiz attempts, and quiz lockout timers.
 *
 * Business Rules:
 * - Lessons must be completed in order.
 * - Quiz attempts are capped at 3; failing 3 times triggers a 24-hour lockout.
 * - Progress percentage is recalculated after every lesson.
 * - If a course is already completed, subsequent lessons added to the course are optional.
 */
public class Progress {
    private final int enrollmentId;
    private final Set<Integer> completedLessonIds;
    private final Map<Integer, Integer> quizAttempts;
    private final Map<Integer, LocalDateTime> quizLockUntil;
    private double progressPercentage;
    private boolean courseCompletedFlag;

    public Progress(int enrollmentId) {
        if (enrollmentId <= 0) {
            throw new IllegalArgumentException("Enrollment ID must be positive.");
        }
        this.enrollmentId = enrollmentId;
        this.completedLessonIds = new HashSet<>();
        this.quizAttempts = new HashMap<>();
        this.quizLockUntil = new HashMap<>();
        this.progressPercentage = 0.0;
        this.courseCompletedFlag = false;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public Set<Integer> getCompletedLessonIds() {
        return Collections.unmodifiableSet(completedLessonIds);
    }

    public int getCompletedLessonCount() {
        return completedLessonIds.size();
    }

    public boolean isLessonCompleted(int lessonId) {
        return completedLessonIds.contains(lessonId);
    }

    public void markLessonCompleted(int lessonId) {
        completedLessonIds.add(lessonId);
    }

    public int getQuizAttempts(int lessonId) {
        return quizAttempts.getOrDefault(lessonId, 0);
    }

    /**
     * Increments and returns the updated attempt count for the specified quiz lesson.
     */
    public int recordQuizAttempt(int lessonId) {
        int current = getQuizAttempts(lessonId) + 1;
        quizAttempts.put(lessonId, current);
        return current;
    }

    /**
     * Resets attempts (e.g. after lockout expires or on pass).
     */
    public void resetQuizAttempts(int lessonId) {
        quizAttempts.remove(lessonId);
        quizLockUntil.remove(lessonId);
    }

    /**
     * Checks if a quiz lesson is currently locked out due to exceeding maximum attempts.
     */
    public boolean isQuizLocked(int lessonId) {
        LocalDateTime lockTime = quizLockUntil.get(lessonId);
        if (lockTime == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(lockTime)) {
            // Lockout has expired, reset attempts and lock
            quizLockUntil.remove(lessonId);
            quizAttempts.put(lessonId, 0);
            return false;
        }
        return true;
    }

    public LocalDateTime getQuizLockoutExpiry(int lessonId) {
        return quizLockUntil.get(lessonId);
    }

    /**
     * Locks the quiz for 24 hours.
     */
    public void lockQuizFor24Hours(int lessonId) {
        quizLockUntil.put(lessonId, LocalDateTime.now().plusHours(24));
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = Math.min(100.0, Math.max(0.0, progressPercentage));
    }

    public boolean isCourseCompletedFlag() {
        return courseCompletedFlag;
    }

    public void setCourseCompletedFlag(boolean courseCompletedFlag) {
        this.courseCompletedFlag = courseCompletedFlag;
    }

    @Override
    public String toString() {
        return String.format("Progress [Enrollment #%d]: %d lessons completed (%.1f%%)",
                enrollmentId, completedLessonIds.size(), progressPercentage);
    }
}
