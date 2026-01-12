package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Chat;
import dev.studylink.studylink.business.User;

import java.util.List;
import java.util.Optional;

public interface ChatDAO {
    /**
     * Crée un nouveau chat
     */
    boolean createChat(Chat chat);

    /**
     * Trouve un chat par son ID
     */
    Optional<Chat> findById(int id);

    /**
     * Récupère tous les chats de l'utilisateur
     */
    List<Chat> getChatsByUser(int userId);

    /**
     * Met à jour un chat
     */
    boolean updateChat(Chat chat);

    /**
     * Supprime (désactive) un chat
     */
    boolean deleteChat(int chatId);

    /**
     * Ajoute un participant au chat
     */
    boolean addParticipant(int chatId, int userId);

    /**
     * Retire un participant du chat
     */
    boolean removeParticipant(int chatId, int userId);

    /**
     * Récupère les participants d'un chat
     */
    List<User> getParticipants(int chatId);

    /**
     * Ferme les ressources
     */
    void close();
}