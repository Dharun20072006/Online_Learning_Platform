package onlinelearning.course;

import onlinelearning.lesson.Lesson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a section within a course.
 * A course is divided into sections, and each section contains lessons.
 */
public class Section {
    private final int id;
    private String title;
    private final List<Lesson> lessons;

    public Section(int id, String title) {
        if (id <= 0) {
            throw new IllegalArgumentException("Section ID must be positive.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty.");
        }
        this.id = id;
        this.title = title.trim();
        this.lessons = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty.");
        }
        this.title = title.trim();
    }

    /**
     * Returns an unmodifiable view of lessons in this section.
     */
    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    /**
     * Adds a lesson to this section.
     *
     * @param lesson Lesson to add
     */
    public void addLesson(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null.");
        }
        lessons.add(lesson);
    }

    /**
     * Removes a lesson by ID.
     *
     * @param lessonId ID of lesson to remove
     * @return true if removed, false otherwise
     */
    public boolean removeLesson(int lessonId) {
        return lessons.removeIf(l -> l.getId() == lessonId);
    }

    /**
     * Returns total number of lessons in this section.
     */
    public int getLessonCount() {
        return lessons.size();
    }

    @Override
    public String toString() {
        return String.format("Section #%d: %s (%d lessons)", id, title, lessons.size());
    }
}
