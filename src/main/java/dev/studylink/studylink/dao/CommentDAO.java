package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentDAO {
    /**
     * Create a new comment
     */
    Comment createComment(Comment comment);

    /**
     * Find a comment by its ID
     */
    Optional<Comment> findById(int id);

    /**
     * Delete a comment
     */
    boolean deleteComment(int commentId);

    /**
     * Get all comments for a resource
     */
    List<Comment> getCommentsByResource(int resourceId);

    /**
     * Get all comments by an author
     */
    List<Comment> getCommentsByAuthor(int userId);

    /**
     * Close resources
     */
    void close();
}
