package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.User;
import java.util.List;

public interface BlockDAO {
    /**
     * Bloque un utilisateur dans un chat
     */
    boolean blockUser(int chatId, int blockedId, int blockerId);

    /**
     * Débloque un utilisateur dans un chat
     */
    boolean unblockUser(int chatId, int blockedId, int blockerId);

    /**
     * Vérifie si un utilisateur est bloqué dans un chat
     */
    boolean isUserBlocked(int chatId, int blockerId, int blockedId);

    /**
     * Récupère les utilisateurs bloqués par un utilisateur dans un chat
     */
    List<User> getBlockedUsers(int chatId, int userId);

    /**
     * Ferme les ressources
     */
    void close();
}