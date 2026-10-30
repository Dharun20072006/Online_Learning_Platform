package onlinelearning.enrollment;

/**
 * Enrollment lifecycle statuses.
 * Business Rules:
 * - PENDING_PAYMENT: Awaiting payment for a paid course (expires after 24 hours).
 * - ACTIVE: Active enrollment where student can access and complete lessons.
 * - COMPLETED: All lessons completed (progress reaches 100%).
 * - CANCELLED: Refunded or cancelled enrollment.
 * - EXPIRED: Pending payment was not completed within 24 hours.
 */
public enum EnrollmentStatus {
    PENDING_PAYMENT,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
