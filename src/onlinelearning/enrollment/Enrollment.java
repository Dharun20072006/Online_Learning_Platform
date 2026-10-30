package onlinelearning.enrollment;

import onlinelearning.course.Course;
import onlinelearning.user.Student;

import java.time.LocalDateTime;

/**
 * Represents a student's enrollment in a course.
 * Encapsulates the enrollment lifecycle and locked price.
 *
 * Business Rules:
 * - A student can enroll only in a Published course, and only once in the same course.
 * - An instructor cannot enroll in their own course.
 * - Free course becomes Active immediately.
 * - Paid course starts as Pending Payment and activates upon successful payment.
 * - Pending payment enrollment expires after 24 hours.
 * - Locked price ensures price changes only apply to future enrollments.
 */
public class Enrollment {
    private final int id;
    private final Student student;
    private final Course course;
    private final double lockedPrice; // Locked at time of enrollment
    private EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    public Enrollment(int id, Student student, Course course, EnrollmentStatus status) {
        if (id <= 0) {
            throw new IllegalArgumentException("Enrollment ID must be positive.");
        }
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("EnrollmentStatus cannot be null.");
        }

        this.id = id;
        this.student = student;
        this.course = course;
        this.lockedPrice = course.getPrice();
        this.status = status;
        this.enrolledAt = LocalDateTime.now();
        this.completedAt = null;
    }

    public int getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public double getLockedPrice() {
        return lockedPrice;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public void markActive() {
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void markCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = EnrollmentStatus.CANCELLED;
    }

    public void markExpired() {
        this.status = EnrollmentStatus.EXPIRED;
    }

    /**
     * Checks if this enrollment is pending payment and has exceeded the 24-hour window.
     */
    public boolean isPendingPaymentExpired() {
        return this.status == EnrollmentStatus.PENDING_PAYMENT
                && LocalDateTime.now().isAfter(this.enrolledAt.plusHours(24));
    }

    @Override
    public String toString() {
        return String.format("Enrollment #%d | Student: %s | Course: \"%s\" | Price: ₹%.2f | Status: %s | Enrolled: %s",
                id, student.getName(), course.getTitle(), lockedPrice, status, enrolledAt.toLocalDate());
    }
}
