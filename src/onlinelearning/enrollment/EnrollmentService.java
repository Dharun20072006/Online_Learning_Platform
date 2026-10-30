package onlinelearning.enrollment;

import onlinelearning.course.Course;
import onlinelearning.course.CourseStatus;
import onlinelearning.data.DataStore;
import onlinelearning.user.Student;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing student course enrollments.
 *
 * Business Rules:
 * - A student can enroll only in a Published course.
 * - A student can enroll only once in the same course.
 * - An instructor cannot enroll in their own course.
 * - A free course becomes Active immediately.
 * - A paid course begins as Pending Payment and activates upon successful payment.
 * - A pending payment enrollment expires after 24 hours.
 * - A student can have at most 5 courses in progress (Active, not Completed) at a time.
 */
public class EnrollmentService {
    public static final int MAX_ACTIVE_COURSES = 5;
    private final DataStore dataStore;

    public EnrollmentService(DataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("DataStore cannot be null.");
        }
        this.dataStore = dataStore;
    }

    /**
     * Enrolls a student in a course following all platform business rules.
     *
     * @param student The student enrolling
     * @param course  The target course
     * @return Created Enrollment entity
     */
    public Enrollment enroll(Student student, Course course) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }

        // Rule 1: Course must be Published
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot enroll: Course is not published (Current status: " + course.getStatus() + ").");
        }

        // Rule 2: Student can enroll only once in the same course
        if (dataStore.isStudentEnrolledInCourse(student.getId(), course.getId())) {
            throw new IllegalStateException("Student is already enrolled in this course.");
        }

        // Rule 3: An instructor cannot enroll in their own course
        if (course.getInstructor().getId() == student.getId()) {
            throw new IllegalStateException("An instructor cannot enroll in their own course.");
        }

        // Rule 4: At most 5 courses in progress (Active, not Completed) at a time
        long activeCount = dataStore.getActiveEnrollmentsCountForStudent(student.getId());
        if (activeCount >= MAX_ACTIVE_COURSES) {
            throw new IllegalStateException(String.format(
                    "Enrollment limit reached: Students can have at most %d active courses in progress (currently %d).",
                    MAX_ACTIVE_COURSES, activeCount));
        }

        // Rule 5: Free course becomes Active immediately; paid course becomes Pending Payment
        EnrollmentStatus initialStatus = course.isFree() ? EnrollmentStatus.ACTIVE : EnrollmentStatus.PENDING_PAYMENT;

        int enrollmentId = dataStore.getNextEnrollmentId();
        Enrollment enrollment = new Enrollment(enrollmentId, student, course, initialStatus);
        dataStore.addEnrollment(enrollment);

        // Initialize progress tracker for the enrollment
        dataStore.getProgressForEnrollment(enrollmentId);

        return enrollment;
    }

    /**
     * Activates an enrollment following successful payment.
     */
    public void activateAfterPayment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment cannot be null.");
        }
        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Enrollment is not in PENDING_PAYMENT status.");
        }
        // Verify active course limit again in case student enrolled in others while pending
        long activeCount = dataStore.getActiveEnrollmentsCountForStudent(enrollment.getStudent().getId());
        if (activeCount >= MAX_ACTIVE_COURSES) {
            throw new IllegalStateException(String.format(
                    "Cannot activate enrollment: Active course limit of %d reached.", MAX_ACTIVE_COURSES));
        }

        enrollment.markActive();
    }

    /**
     * Checks all pending enrollments and marks any older than 24 hours as EXPIRED.
     * Business Rule: A Pending Payment enrollment expires after 24 hours.
     */
    public int checkAndExpirePendingEnrollments() {
        int expiredCount = 0;
        for (Enrollment e : dataStore.getAllEnrollments().values()) {
            if (e.isPendingPaymentExpired()) {
                e.markExpired();
                expiredCount++;
            }
        }
        return expiredCount;
    }

    /**
     * Gets all enrollments for a given student.
     */
    public List<Enrollment> getStudentEnrollments(int studentId) {
        return dataStore.getEnrollmentsForStudent(studentId);
    }

    /**
     * Gets active enrollments for a student.
     */
    public List<Enrollment> getActiveEnrollments(int studentId) {
        return dataStore.getEnrollmentsForStudent(studentId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Gets an enrollment by ID.
     */
    public Enrollment getEnrollmentById(int enrollmentId) {
        Enrollment e = dataStore.getEnrollmentById(enrollmentId);
        if (e == null) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId);
        }
        return e;
    }
}
