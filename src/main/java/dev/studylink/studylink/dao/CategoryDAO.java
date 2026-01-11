package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;

import java.util.List;
import java.util.Optional;

public interface CategoryDAO {
    /**
     * Find a category by its ID
     */
    Optional<Category> findById(int id);

    /**
     * Get all categories
     */
    List<Category> getAllCategories();

    /**
     * Get categories by type
     */
    List<Category> getCategoriesByType(CategoryType type);

    /**
     * Create a new category
     */
    boolean createCategory(String title, String description, CategoryType type, int level);

    /**
     * Update a category
     */
    boolean updateCategory(Category category);

    /**
     * Delete a category
     */
    boolean deleteCategory(String selected);

    /**
     * Close resources
     */
    void close();

    boolean deleteCategory(int categoryId);
}