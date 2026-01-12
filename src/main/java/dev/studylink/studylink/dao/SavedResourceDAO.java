package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Resource;

import java.util.List;

public interface SavedResourceDAO {
    /**
     * Save a resource for a user
     */
    boolean saveResource(int userId, int resourceId);

    /**
     * Unsave a resource for a user
     */
    boolean unsaveResource(int userId, int resourceId);

    /**
     * Get all saved resources for a user
     */
    List<Resource> getSavedResources(int userId);

    /**
     * Check if a resource is saved by a user
     */
    boolean isResourceSaved(int userId, int resourceId);

    /**
     * Close resources
     */
    void close();
}
