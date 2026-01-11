package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.CartItem;
import java.util.List;

public interface CartDAO {
    /**
     * Ajouter une ressource au panier
     */
    boolean addToCart(int userId, int resourceId, int quantity);
    
    /**
     * Ajouter une session de tutorat au panier
     */
    boolean addSessionToCart(int userId, int sessionId, int quantity);
    
    /**
     * Récupérer tous les items du panier d'un utilisateur
     */
    List<CartItem> getCartItems(int userId);
    
    /**
     * Mettre à jour la quantité d'un item
     */
    boolean updateQuantity(int cartItemId, int quantity);
    
    /**
     * Supprimer un item du panier
     */
    boolean removeFromCart(int cartItemId);
    
    /**
     * Vider le panier d'un utilisateur
     */
    boolean clearCart(int userId);
    
    /**
     * Obtenir le nombre total d'items dans le panier
     */
    int getCartItemCount(int userId);
    
    /**
     * Obtenir le total du panier
     */
    double getCartTotal(int userId);
    
    void close();
}
