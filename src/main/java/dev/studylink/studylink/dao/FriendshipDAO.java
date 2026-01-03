package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.User;

import java.util.List;

public interface FriendshipDAO {
    /**
     * Create a new friendship
     */
    boolean createFriendship(int userId1, int userId2);

    /**
     * Remove a friendship
     */
    boolean removeFriendship(int userId1, int userId2);

    /**
     * Get all friends for a user
     */
    List<User> getFriendsForUser(int userId);

    /**
     * Check if two users are friends
     */
    boolean areFriends(int userId1, int userId2);

    /**
     * Close resources
     */
    void close();
}