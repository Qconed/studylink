package dev.studylink.studylink.dao;

// choix d'une class abstraite plutôt qu'une interface, dans le cas ou
// besoin de configuration commune aux Produits de la Factory
public interface UserFactory {
    /**
     * Crée une instance de UserDAO
     */
    public UserDAO createUserDAO();

    /**
     * Create a FriendRequestDAO instance
     */
    FriendRequestDAO createFriendRequestDAO();

    /**
     * Create a FriendshipDAO instance
     */
    FriendshipDAO createFriendshipDAO();

    /**
     * Create a CategoryDAO instance
     */
    CategoryDAO createCategoryDAO();
    
    public PostDAO createPostDAO();
}
