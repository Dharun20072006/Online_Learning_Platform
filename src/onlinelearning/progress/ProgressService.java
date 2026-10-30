package onlinelearning.progress;

import onlinelearning.certificate.Certificate;
import onlinelearning.course.Course;
import onlinelearning.data.DataStore;
import onlinelearning.enrollment.Enrollment;
import onlinelearning.enrollment.EnrollmentStatus;
import onlinelearning.lesson.Lesson;
import onlinelearning.lesson.QuizLesson;
import onlinelearning.lesson.VideoLesson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service managing student lesson progression and completion logic.
 *
 * Business Rules:
 * - Lessons must be completed in order. A student cannot start a lesson until the previous one is completed.
 * - Video lesson: watched at least 90% of its duration.
 * - Quiz lesson: score >= 60%; at most 3 attempts; 24-hour lockout after 3 failed attempts.
 * - Progress percentage = (completed lessons / total lessons) * 100.
 * - A course is Completed when progress reaches 100%, and completion date is recorded.
 * - If new lessons are added after a student completed the course, enrollment stays Completed
 *   and new lessons are optional for that student.
 */
public class ProgressService {
    private final DataStore dataStore;

    public ProgressService(DataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("DataStore cannot be null.");
        }
        this.dataStore = dataStore;
    }

    /**
     * Attempts to complete a specific lesson for an active enrollment.
     *
     * @param enrollment     The active course enrollment
     * @param lessonId       The ID of the lesson to complete
     * @param watchedMinutes Number of minutes watched (for VideoLesson)
     * @param quizScore      Percentage score achieved (for QuizLesson, 0-100)
     * @return Updated progress percentage (0.0 to 100.0)
     */
    public double completeLesson(Enrollment enrollment, int lessonId, int watchedMinutes, double quizScore) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null.");
        }

        // Rule 1: Enrollment must be Active or Completed (if taking optional new lessons)
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot take lessons: Enrollment status is " + enrollment.getStatus());
        }

        Course course = enrollment.getCourse();
        List<Lesson> orderedLessons = course.getAllLessonsOrdered();
        if (orderedLessons.isEmpty()) {
            throw new IllegalStateException("Course has no lessons available.");
        }

        // Find lesson index in the ordered sequence
        int targetIndex = -1;
        for (int i = 0; i < orderedLessons.size(); i++) {
            if (orderedLessons.get(i).getId() == lessonId) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            throw new IllegalArgumentException("Lesson ID " + lessonId + " not found in course \"" + course.getTitle() + "\"");
        }

        Progress progress = dataStore.getProgressForEnrollment(enrollment.getId());

        // Already completed check
        if (progress.isLessonCompleted(lessonId)) {
            return progress.getProgressPercentage();
        }

        // Rule 2: Lessons must be completed in order. Cannot start lesson until previous one is completed.
        for (int i = 0; i < targetIndex; i++) {
            Lesson prev = orderedLessons.get(i);
            if (!progress.isLessonCompleted(prev.getId())) {
                throw new IllegalStateException(String.format(
                        "Sequential Progress Required: Please complete lesson #%d (\"%s\") first before starting lesson #%d.",
                        prev.getOrder(), prev.getTitle(), orderedLessons.get(targetIndex).getOrder()));
            }
        }

        Lesson currentLesson = orderedLessons.get(targetIndex);

        // Rule 3: Quiz specific rules (max 3 attempts, 24h lockout, score >= 60%)
        if (currentLesson instanceof QuizLesson quiz) {
            if (progress.isQuizLocked(lessonId)) {
                LocalDateTime lockExpiry = progress.getQuizLockoutExpiry(lessonId);
                String expiryStr = lockExpiry != null
                        ? lockExpiry.format(DateTimeFormatter.ofPattern("HH:mm dd-MMM-yyyy"))
                        : "24 hours";
                throw new IllegalStateException(String.format(
                        "Quiz is locked due to 3 failed attempts. Please wait until %s before trying again.", expiryStr));
            }

            int attempt = progress.recordQuizAttempt(lessonId);

            if (!quiz.isCompleted(watchedMinutes, quizScore)) {
                if (attempt >= QuizLesson.MAX_ATTEMPTS) {
                    progress.lockQuizFor24Hours(lessonId);
                    throw new IllegalStateException(String.format(
                            "Quiz failed (Score: %.1f%%). Maximum attempts (%d) reached. Quiz is locked for 24 hours.",
                            quizScore, QuizLesson.MAX_ATTEMPTS));
                } else {
                    int remaining = QuizLesson.MAX_ATTEMPTS - attempt;
                    throw new IllegalStateException(String.format(
                            "Quiz failed (Score: %.1f%%, minimum 60%% required). Attempt %d of %d. You have %d attempt(s) remaining.",
                            quizScore, attempt, QuizLesson.MAX_ATTEMPTS, remaining));
                }
            }

            // Passed quiz: reset attempts
            progress.resetQuizAttempts(lessonId);
        } else if (currentLesson instanceof VideoLesson video) {
            // Rule 4: Video lesson requires >= 90% watched
            int requiredMinutes = (int) Math.ceil(video.getDurationMinutes() * 0.90);
            if (!video.isCompleted(watchedMinutes, quizScore)) {
                throw new IllegalStateException(String.format(
                        "Video lesson not completed: Watched %d mins, but at least 90%% (%d mins of %d mins) is required.",
                        watchedMinutes, requiredMinutes, video.getDurationMinutes()));
            }
        } else {
            // ReadingLesson or other
            if (!currentLesson.isCompleted(watchedMinutes, quizScore)) {
                throw new IllegalStateException("Lesson completion criteria not met.");
            }
        }

        // Mark lesson as complete
        progress.markLessonCompleted(lessonId);

        // Rule 5: Progress percentage = (completed lessons / total lessons) * 100.
        int totalLessons = orderedLessons.size();
        int completedLessons = progress.getCompletedLessonCount();
        double percentage = (completedLessons * 100.0) / totalLessons;
        progress.setProgressPercentage(percentage);

        // Rule 6: Course is Completed when progress reaches 100%
        if (percentage >= 100.0) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                enrollment.markCompleted();
                progress.setCourseCompletedFlag(true);

                // Auto-issue unique Certificate upon course completion!
                if (dataStore.getCertificateForEnrollment(enrollment.getId()) == null) {
                    Certificate cert = Certificate.issueFor(enrollment);
                    dataStore.addCertificate(cert);
                }
            }
        }

        return percentage;
    }

    /**
     * Gets progress record for an enrollment.
     */
    public Progress getProgress(int enrollmentId) {
        return dataStore.getProgressForEnrollment(enrollmentId);
    }
}
