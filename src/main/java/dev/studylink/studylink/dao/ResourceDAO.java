package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Resource;

import java.util.List;
import java.util.Optional;

public interface ResourceDAO {
    /**
     * Create a new resource
     */
    boolean createResource(Resource resource);

    /**
     * Find a resource by its ID
     */
    Optional<Resource> findById(int id);

    /**
     * Update a resource
     */
    boolean updateResource(Resource resource);

    /**
     * Delete a resource
     */
    boolean deleteResource(int resourceId);

    /**
     * Get all resources
     */
    List<Resource> getAllResources();

    /**
     * Get resources by owner
     */
    List<Resource> getResourcesByOwner(int userId);

    /**
     * Get resources by category
     */
    List<Resource> getResourcesByCategory(int categoryId);

    /**
     * Search resources by query (title or content)
     */
    List<Resource> searchResources(String query);

    /**
     * Add a category to a resource
     */
    boolean addCategoryToResource(int resourceId, int categoryId);

    /**
     * Remove a category from a resource
     */
    boolean removeCategoryFromResource(int resourceId, int categoryId);

    /**
     * Get all categories for a resource
     */
    List<Category> getResourceCategories(int resourceId);

    /**
     * Increment view count
     */
    boolean incrementViewCount(int resourceId);

    /**
     * Check if a user has already viewed a resource
     */
    boolean hasUserViewedResource(int userId, int resourceId);

    /**
     * Record that a user has viewed a resource
     */
    boolean recordResourceView(int userId, int resourceId);

    /**
     * Increment save count
     */
    boolean incrementSaveCount(int resourceId);

    /**
     * Decrement save count
     */
    boolean decrementSaveCount(int resourceId);

    /**
     * Close resources
     */
    void close();
}
