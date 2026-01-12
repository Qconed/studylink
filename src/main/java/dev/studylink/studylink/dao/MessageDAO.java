package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Message;

import java.util.List;
import java.util.Optional;

public interface MessageDAO {
    /**
     * Crée un nouveau message
     */
    boolean createMessage(Message message);

    /**
     * Trouve un message par son ID
     */
    Optional<Message> findById(int id);

    /**
     * Récupère les messages d'un chat (paginated)
     * @param chatId ID du chat
     * @param limit Nombre de messages à récupérer (ex: 50)
     * @param offset Décalage pour pagination
     */
    List<Message> getMessagesByChat(int chatId, int limit, int offset);

    /**
     * Met à jour un message (contenu, édité_at)
     */
    boolean updateMessage(Message message);

    /**
     * Supprime un message (soft delete)
     */
    boolean deleteMessage(int messageId);

    /**
     * Ferme les ressources
     */
    void close();
}