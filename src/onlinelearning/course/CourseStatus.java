package onlinelearning.course;

/**
 * Lifecycle status of a course on the platform.
 * DRAFT: Newly created, can be edited and published.
 * PUBLISHED: Available for students to enroll.
 * WITHDRAWN: Withdrawn by admin; all enrolled students get a full refund.
 */
public enum CourseStatus {
    DRAFT,
    PUBLISHED,
    WITHDRAWN
}
