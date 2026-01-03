package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.FriendRequest;
import dev.studylink.studylink.business.FriendRequestStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRequestDAO {
    /**
     * Create a new friend request
     */
    FriendRequest createFriendRequest(int senderId, int receiverId);

    /**
     * Find a friend request by ID
     */
    Optional<FriendRequest> findById(int requestId);

    /**
     * Update the status of a friend request
     */
    boolean updateRequestStatus(int requestId, FriendRequestStatus status);

    /**
     * Get all pending requests for a user
     */
    List<FriendRequest> getPendingRequestsForUser(int userId);

    /**
     * Get all sent requests by a user
     */
    List<FriendRequest> getSentRequestsByUser(int userId);

    /**
     * Check if two users are already friends
     */
    boolean areFriends(int userId1, int userId2);

    /**
     * Check if a friend request already exists between two users
     */
    boolean requestExists(int senderId, int receiverId);

    /**
     * Close resources
     */
    void close();
}