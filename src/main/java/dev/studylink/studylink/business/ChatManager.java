package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.*;
import dev.studylink.studylink.impl.db.mysql.MySQLChatFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Core business logic pour le Chat System
 * Singleton pattern identique à UserManager
 */
public class ChatManager {
    private static final Logger log = Logger.getLogger(ChatManager.class.getName());
    private static ChatManager instance;
    private ChatFactory chatFactory;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private BlockDAO blockDAO;
    private NotificationDAO notificationDAO;

    private ChatManager(ChatFactory chatFactory) {
        this.chatFactory = chatFactory;
        this.chatDAO = chatFactory.createChatDAO();
        this.messageDAO = chatFactory.createMessageDAO();
        this.blockDAO = chatFactory.createBlockDAO();
        this.notificationDAO = chatFactory.createNotificationDAO();
    }

    public static ChatManager getInstance(ChatFactory chatFactory) {
        if (instance == null) {
            instance = new ChatManager(chatFactory);
        }
        return instance;
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager(MySQLChatFactory.getInstance());
        }
        return instance;
    }

    // ===== CHAT OPERATIONS =====

    /**
     * Crée un chat privé entre deux utilisateurs
     */
    public Chat createPrivateChat(User user1, User user2) {
        // Vérifier qu'aucun chat privé n'existe déjà
        List<Chat> existingChats = chatDAO.getChatsByUser(user1.getId());
        for (Chat chat : existingChats) {
            if (chat.isPrivate()) {
                for (User participant : chat.getParticipants()) {
                    if (participant.getId() == user2.getId()) {
                        log.info("Chat privé existe déjà entre " + user1.getId() + " et " + user2.getId());
                        return chat;  // Chat existe déjà
                    }
                }
            }
        }

        // Créer le nouveau chat
        Chat chat = new Chat();
        chat.setType(ChatType.PRIVATE);
        chat.setCreatedById(user1.getId());
        chat.addParticipant(user1);
        chat.addParticipant(user2);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setLastMessageAt(LocalDateTime.now());

        if (chatDAO.createChat(chat)) {
            log.info("Chat privé créé: " + chat.getId());
            return chat;
        }

        log.severe("Erreur lors de la création du chat privé");
        return null;
    }

    /**
     * Crée un chat groupe
     */
    public Chat createGroupChat(String groupName, User creator, List<User> participants) {
        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setName(groupName);
        chat.setCreatedById(creator.getId());
        chat.addParticipant(creator);

        for (User participant : participants) {
            if (!chat.getParticipants().contains(participant)) {
                chat.addParticipant(participant);
            }
        }

        chat.setCreatedAt(LocalDateTime.now());
        chat.setLastMessageAt(LocalDateTime.now());

        if (chatDAO.createChat(chat)) {
            // Notifier les participants
            for (User participant : participants) {
                if (participant.getId() != creator.getId()) {
                    Notification notif = new Notification(
                            participant.getId(),
                            NotificationType.USER_JOINED,
                            chat.getId()
                    );
                    notificationDAO.createNotification(notif);
                }
            }
            log.info("Chat groupe créé: " + chat.getId());
            return chat;
        }

        log.severe("Erreur lors de la création du chat groupe");
        return null;
    }

    /**
     * Récupère tous les chats de l'utilisateur
     */
    public List<Chat> getChatsForUser(int userId) {
        return chatDAO.getChatsByUser(userId);
    }

    /**
     * Récupère un chat par son ID
     */
    public Optional<Chat> getChatById(int chatId) {
        return chatDAO.findById(chatId);
    }

    // ===== MESSAGE OPERATIONS =====

    /**
     * Envoie un message dans un chat
     */
    public Message sendMessage(int userId, int chatId, String content) throws Exception {
        // Validation
        if (content == null || content.trim().isEmpty()) {
            throw new Exception("Le contenu du message ne peut pas être vide");
        }

        if (content.length() > 5000) {
            throw new Exception("Le message est trop long (max 5000 caractères)");
        }

        // Vérifier que l'utilisateur est dans le chat
        Chat chat = chatDAO.findById(chatId)
                .orElseThrow(() -> new Exception("Chat introuvable"));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(u -> u.getId() == userId);

        if (!isParticipant) {
            throw new Exception("Vous n'êtes pas participant de ce chat");
        }

        // Vérifier le blocage (est-ce que cet utilisateur est bloqué par quelqu'un?)
        for (User participant : chat.getParticipants()) {
            if (blockDAO.isUserBlocked(chatId, participant.getId(), userId)) {
                throw new Exception("Vous êtes bloqué dans ce chat");
            }
        }

        // Créer le message
        Message message = new Message(chatId, userId, content);
        if (messageDAO.createMessage(message)) {
            // Mettre à jour le chat
            chat.setLastMessageAt(LocalDateTime.now());
            chatDAO.updateChat(chat);

            // Notifier les autres participants
            for (User participant : chat.getParticipants()) {
                if (participant.getId() != userId) {
                    Notification notif = new Notification(
                            participant.getId(),
                            NotificationType.NEW_MESSAGE,
                            chatId
                    );
                    notif.setRelatedMessageId(message.getId());
                    notificationDAO.createNotification(notif);
                }
            }

            log.info("Message envoyé: " + message.getId());
            return message;
        }

        throw new Exception("Erreur lors de l'envoi du message");
    }

    /**
     * Récupère les messages d'un chat (paginated)
     */
    public List<Message> getMessages(int chatId, int page) throws Exception {
        // Vérifier que le chat existe
        chatDAO.findById(chatId)
                .orElseThrow(() -> new Exception("Chat introuvable"));

        int limit = 50;  // Messages par page
        int offset = (page - 1) * limit;
        return messageDAO.getMessagesByChat(chatId, limit, offset);
    }

    /**
     * Édite un message
     */
    public boolean editMessage(int messageId, String newContent, int userId) throws Exception {
        Message message = messageDAO.findById(messageId)
                .orElseThrow(() -> new Exception("Message introuvable"));

        // Vérifier la propriété
        if (message.getSenderId() != userId) {
            throw new Exception("Vous ne pouvez éditer que vos propres messages");
        }

        // Vérifier le délai d'édition (5 minutes)
        LocalDateTime createdTime = message.getCreatedAt();
        long minutesPassed = ChronoUnit.MINUTES.between(createdTime, LocalDateTime.now());

        if (minutesPassed > 5) {
            throw new Exception("Vous pouvez éditer les messages seulement pendant 5 minutes");
        }

        message.editContent(newContent);

        if (messageDAO.updateMessage(message)) {
            // Notifier de l'édition
            Chat chat = chatDAO.findById(message.getChatId())
                    .orElseThrow(() -> new Exception("Chat introuvable"));

            for (User participant : chat.getParticipants()) {
                if (participant.getId() != userId) {
                    Notification notif = new Notification(
                            participant.getId(),
                            NotificationType.MESSAGE_EDITED,
                            message.getChatId()
                    );
                    notif.setRelatedMessageId(messageId);
                    notificationDAO.createNotification(notif);
                }
            }

            log.info("Message édité: " + messageId);
            return true;
        }

        throw new Exception("Erreur lors de la modification du message");
    }

    /**
     * Supprime un message
     */
    public boolean deleteMessage(int messageId, int userId) throws Exception {
        Message message = messageDAO.findById(messageId)
                .orElseThrow(() -> new Exception("Message introuvable"));

        if (message.getSenderId() != userId) {
            throw new Exception("Vous ne pouvez supprimer que vos propres messages");
        }

        log.info("Message supprimé: " + messageId);
        return messageDAO.deleteMessage(messageId);
    }

    // ===== BLOCKING OPERATIONS =====

    /**
     * Bloque un utilisateur dans un chat
     */
    public boolean blockUser(int chatId, int blockedId, int blockerId) throws Exception {
        // Vérifier que le chat existe
        Chat chat = chatDAO.findById(chatId)
                .orElseThrow(() -> new Exception("Chat introuvable"));

        // Vérifier que les deux utilisateurs sont dans le chat
        boolean blockerExists = chat.getParticipants().stream()
                .anyMatch(u -> u.getId() == blockerId);
        boolean blockedExists = chat.getParticipants().stream()
                .anyMatch(u -> u.getId() == blockedId);

        if (!blockerExists || !blockedExists) {
            throw new Exception("Les deux utilisateurs doivent être dans le chat");
        }

        log.info("Utilisateur " + blockedId + " bloqué par " + blockerId + " dans chat " + chatId);
        return blockDAO.blockUser(chatId, blockedId, blockerId);
    }

    /**
     * Débloque un utilisateur dans un chat
     */
    public boolean unblockUser(int chatId, int unblockedId, int blockerId) throws Exception {
        chatDAO.findById(chatId)
                .orElseThrow(() -> new Exception("Chat introuvable"));

        log.info("Utilisateur " + unblockedId + " débloqué par " + blockerId + " dans chat " + chatId);
        return blockDAO.unblockUser(chatId, unblockedId, blockerId);
    }

    /**
     * Récupère les utilisateurs bloqués par un utilisateur
     */
    public List<User> getBlockedUsers(int chatId, int userId) {
        return blockDAO.getBlockedUsers(chatId, userId);
    }

    // ===== NOTIFICATION OPERATIONS =====

    /**
     * Récupère les notifications non-lues de l'utilisateur
     */
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDAO.findUnreadByUserId(userId);
    }

    /**
     * Marque une notification comme lue
     */
    public void markNotificationAsRead(int notificationId) {
        notificationDAO.markAsRead(notificationId);
    }
}