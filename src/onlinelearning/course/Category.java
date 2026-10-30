package onlinelearning.course;

/**
 * Categories available for courses on the platform.
 * Requirement: Programming, Design, Business, Data Science, or Language.
 */
public enum Category {
    PROGRAMMING("Programming"),
    DESIGN("Design"),
    BUSINESS("Business"),
    DATA_SCIENCE("Data Science"),
    LANGUAGE("Language");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
