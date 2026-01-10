package dev.studylink.studylink.dao;

public interface ResourceFactory {
    /**
     * Create a ResourceDAO instance
     */
    ResourceDAO createResourceDAO();

    /**
     * Create a CommentDAO instance
     */
    CommentDAO createCommentDAO();

    /**
     * Create a SavedResourceDAO instance
     */
    SavedResourceDAO createSavedResourceDAO();
}
