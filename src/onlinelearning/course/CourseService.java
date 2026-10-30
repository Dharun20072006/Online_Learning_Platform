package onlinelearning.course;

import onlinelearning.data.DataStore;
import onlinelearning.lesson.Lesson;
import onlinelearning.user.Admin;
import onlinelearning.user.Instructor;
import onlinelearning.user.Student;
import onlinelearning.user.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling Course Management business logic.
 * Assigned to Member 1 (User & Course Management Modules).
 *
 * Business Rules:
 * - Only an Instructor or Admin can create a course. Students cannot.
 * - Course must have title, category, level, and price (>= 0; 0 is free).
 * - A course can be published only if it has at least one section and >= 3 lessons.
 * - Once published, category and level CANNOT be changed.
 * - Title and description can be edited, and new lessons added after publishing.
 * - A lesson cannot be deleted once any student is enrolled in the course.
 * - Price changes apply only to new enrollments.
 */
public class CourseService {
    private final DataStore dataStore;

    public CourseService(DataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("DataStore cannot be null.");
        }
        this.dataStore = dataStore;
    }

    /**
     * Creates a new course in DRAFT status.
     * Enforces the rule that only an Instructor or Admin can create courses.
     *
     * @param creator     The user attempting to create the course
     * @param title       Course title
     * @param description Course description
     * @param category    Category (Programming, Design, Business, Data Science, Language)
     * @param level       Level (Beginner, Intermediate, Advanced)
     * @param price       Price in rupees (0 means free)
     * @param assignedInstructor Assigned instructor if creator is Admin, or creator if Instructor
     * @return Created Course
     */
    public Course createCourse(User creator, String title, String description,
                               Category category, Level level, double price,
                               Instructor assignedInstructor) {
        if (creator == null) {
            throw new IllegalArgumentException("Creator user cannot be null.");
        }
        if (creator instanceof Student) {
            throw new SecurityException("Permission Denied: Students are not permitted to create courses.");
        }
        if (!(creator instanceof Instructor) && !(creator instanceof Admin)) {
            throw new SecurityException("Permission Denied: Only Instructors or Admins can create courses.");
        }

        Instructor owner;
        if (creator instanceof Instructor inst) {
            owner = inst;
        } else {
            // Creator is Admin; must have an assigned instructor
            if (assignedInstructor == null) {
                // If not explicitly passed, assign the first available instructor
                owner = dataStore.getAllInstructors().values().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Cannot create course: No registered instructors found."));
            } else {
                owner = assignedInstructor;
            }
        }

        int courseId = dataStore.getNextCourseId();
        Course course = new Course(courseId, title, description, category, level, price, owner);
        dataStore.addCourse(course);
        return course;
    }

    /**
     * Adds a new section to an existing course.
     */
    public Section addSection(Course course, String sectionTitle) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getStatus() == CourseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot modify a withdrawn course.");
        }
        int sectionId = dataStore.getNextSectionId();
        Section section = new Section(sectionId, sectionTitle);
        course.addSection(section);
        return section;
    }

    /**
     * Adds a lesson to a specific section within a course.
     * Business Rule: New lessons CAN be added even after publishing.
     */
    public void addLessonToSection(Course course, int sectionId, Lesson lesson) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getStatus() == CourseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot add lessons to a withdrawn course.");
        }
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null.");
        }

        Section targetSection = course.getSections().stream()
                .filter(s -> s.getId() == sectionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Section with ID " + sectionId + " not found in course."));

        targetSection.addLesson(lesson);
    }

    /**
     * Publishes a course making it available for enrollment.
     * Business Rule: Requires at least one section and at least 3 lessons across the course.
     */
    public void publishCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        course.publish();
    }

    /**
     * Edits course title and description.
     * Business Rule: Category and Level cannot be changed once published, but
     * title and description can be edited at any time.
     */
    public void editCourse(Course course, String newTitle, String newDescription) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getStatus() == CourseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot edit a withdrawn course.");
        }
        course.edit(newTitle, newDescription);
    }

    /**
     * Updates course price.
     * Business Rule: Price change applies only to new enrollments.
     */
    public void updateCoursePrice(Course course, double newPrice) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (course.getStatus() == CourseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot change price of a withdrawn course.");
        }
        course.changePrice(newPrice);
    }

    /**
     * Deletes a lesson from a course section.
     * Business Rule: A lesson cannot be deleted once any student is enrolled in the course.
     */
    public void deleteLesson(Course course, int sectionId, int lessonId) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        // Business Rule validation: check if any student has enrolled in the course
        if (dataStore.hasEnrollmentsForCourse(course.getId())) {
            throw new IllegalStateException("Cannot delete lesson: Students are already enrolled in this course.");
        }

        Section section = course.getSections().stream()
                .filter(s -> s.getId() == sectionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Section ID " + sectionId + " not found."));

        boolean removed = section.removeLesson(lessonId);
        if (!removed) {
            throw new IllegalArgumentException("Lesson ID " + lessonId + " not found in section " + sectionId);
        }
    }

    /**
     * Retrieves all published courses available for enrollment.
     */
    public List<Course> getPublishedCourses() {
        return dataStore.getPublishedCourses();
    }

    /**
     * Retrieves all courses on the platform.
     */
    public List<Course> getAllCourses() {
        return List.copyOf(dataStore.getAllCourses().values());
    }

    /**
     * Retrieves courses taught by a specific instructor.
     */
    public List<Course> getCoursesByInstructor(int instructorId) {
        return dataStore.getAllCourses().values().stream()
                .filter(c -> c.getInstructor().getId() == instructorId)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves course by ID.
     */
    public Course getCourseById(int courseId) {
        Course c = dataStore.getCourseById(courseId);
        if (c == null) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }
        return c;
    }
}
