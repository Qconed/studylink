package dev.studylink.studylink.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String fullname;
    private String passwordHash;
    private String email;
    private Role role;
    private String bio;
    private String avatarPath;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private boolean isSuspended;
    private String suspensionReason;
    private List<Category> categories;



    public User(String fullname, String passwordHash, String email) {
        this.id = 0;
        this.fullname = fullname;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = Role.STUDENT;
        this.bio = "";
        this.avatarPath = null;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = null;
        this.isSuspended = false;
        this.suspensionReason = null;
        this.categories = new ArrayList<>();
    }
    // pour recuperation depuis la BDD
    public User(int id, String fullname, String email, String passwordHash) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = Role.STUDENT;
        this.bio = "";
        this.avatarPath = null;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = null;
        this.isSuspended = false;
        this.suspensionReason = null;
        this.categories = new ArrayList<>();
    }
    public User(int id, String fullname, String email, String passwordHash, Role role,
                String bio, String avatarPath, LocalDateTime createdAt, LocalDateTime lastLoginAt,
                boolean isSuspended, String suspensionReason) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.bio = bio;
        this.avatarPath = avatarPath;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.isSuspended = isSuspended;
        this.suspensionReason = suspensionReason;
        this.categories = new ArrayList<>();
    }
    // GETTERS & SETTERS -----------------------
    public int getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setSuspended(boolean suspended) {
        this.isSuspended = suspended;
    }

    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    public void setCategories(List<Category> categories) {
        this.categories = new ArrayList<>(categories);
    }

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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
