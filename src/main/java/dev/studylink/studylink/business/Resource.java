package dev.studylink.studylink.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Resource {
    private int id;
    private String title;
    private String content;
    private String attachmentPath;
    private double price;
    private int ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private int saveCount;
    private List<Category> categories;
    private String ownerName;

    // Constructor without ID (for creation)
    public Resource(String title, String content, int ownerId) {
        this.id = 0;
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.attachmentPath = null;
        this.price = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.saveCount = 0;
        this.categories = new ArrayList<>();
        this.ownerName = null;
    }

    // Constructor with ID (from database)
    public Resource(int id, String title, String content, int ownerId, String attachmentPath, double price) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.attachmentPath = attachmentPath;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0;
        this.saveCount = 0;
        this.categories = new ArrayList<>();
        this.ownerName = null;
    }

    // Full constructor (from database)
    public Resource(int id, String title, String content, int ownerId, String attachmentPath,
                    double price, LocalDateTime createdAt, LocalDateTime updatedAt,
                    int viewCount, int saveCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.attachmentPath = attachmentPath;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.saveCount = saveCount;
        this.categories = new ArrayList<>();
        this.ownerName = null;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public double getPrice() {
        return price;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() { return ownerName; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getSaveCount() {
        return saveCount;
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void setSaveCount(int saveCount) {
        this.saveCount = saveCount;
    }

    public void setCategories(List<Category> categories) {
        this.categories = new ArrayList<>(categories);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Category management
    public void addCategory(Category category) {
        if (!hasCategory(category.getId())) {
            this.categories.add(category);
        }
    }

    public boolean removeCategory(int categoryId) {
        return this.categories.removeIf(c -> c.getId() == categoryId);
    }

    public boolean hasCategory(int categoryId) {
        return this.categories.stream().anyMatch(c -> c.getId() == categoryId);
    }

    // Business methods
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSaveCount() {
        this.saveCount++;
    }

    public boolean isFree() {
        return this.price == 0.0;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ownerId=" + ownerId +
                ", ownerName='" + ownerName + '\'' +
                ", price=" + price +
                ", viewCount=" + viewCount +
                ", saveCount=" + saveCount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Resource resource = (Resource) obj;
        return id == resource.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
