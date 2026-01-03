package dev.studylink.studylink.business;

public class Category {
    private int id;
    private String title;
    private String description;
    private CategoryType type;
    private int level;

    // Constructor without ID (for creation)
    public Category(String title, String description, CategoryType type) {
        this.id = 0;
        this.title = title;
        this.description = description;
        this.type = type;
        this.level = 0;
    }

    // Constructor with ID (from database)
    public Category(int id, String title, String description, CategoryType type, int level) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.level = level;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public CategoryType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return title + (level > 0 ? " (Level " + level + ")" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}