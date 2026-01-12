package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Notification;
import dev.studylink.studylink.business.SessionFacade;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour la vue des notifications
 */
public class NotificationsController {

    @FXML
    private VBox notificationsContainer;

    @FXML
    private VBox emptyStateContainer;

    @FXML
    private Label unreadCountLabel;

    @FXML
    private Button markAllReadButton;

    @FXML
    private Button allFilterButton;

    @FXML
    private Button unreadFilterButton;

    private SessionFacade sessionFacade;
    private boolean showOnlyUnread = false;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        sessionFacade = SessionFacade.getInstance();
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Charge et affiche les notifications
     */
    private void loadNotifications() {
        notificationsContainer.getChildren().clear();

        List<Notification> notifications;
        if (showOnlyUnread) {
            notifications = sessionFacade.getMyUnreadNotifications();
        } else {
            notifications = sessionFacade.getMyNotifications();
        }

        if (notifications.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            for (Notification notification : notifications) {
                notificationsContainer.getChildren().add(createNotificationCard(notification));
            }
        }
    }

    /**
     * Crée une carte pour une notification
     */
    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(getCardStyle(notification.isRead()));

        // Header avec timestamp et bouton marquer comme lu
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(notification.getTimestamp().format(DATE_FORMATTER));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(5);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton marquer comme lu (seulement si non lu)
        if (!notification.isRead()) {
            Button markReadButton = new Button("✓");
            markReadButton.setStyle("-fx-background-color: #356ee9; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            markReadButton.setOnAction(e -> markAsRead(notification.getId()));
            actions.getChildren().add(markReadButton);
        }

        // Bouton supprimer
        Button deleteButton = new Button("🗑");
        deleteButton.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> deleteNotification(notification.getId()));
        actions.getChildren().add(deleteButton);

        header.getChildren().addAll(timeLabel, spacer, actions);

        // Contenu de la notification
        Label contentLabel = new Label(notification.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1E293B;");

        // Badge "Non lue" si applicable
        if (!notification.isRead()) {
            Label unreadBadge = new Label("● Nouvelle");
            unreadBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #356ee9; -fx-font-weight: bold;");
            card.getChildren().addAll(header, contentLabel, unreadBadge);
        } else {
            card.getChildren().addAll(header, contentLabel);
        }

        return card;
    }

    /**
     * Retourne le style CSS pour la carte en fonction du statut de lecture
     */
    private String getCardStyle(boolean isRead) {
        if (isRead) {
            return "-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 10;";
        } else {
            return "-fx-background-color: #EFF6FF; -fx-background-radius: 10; -fx-border-color: #356ee9; -fx-border-width: 2; -fx-border-radius: 10;";
        }
    }

    /**
     * Marque une notification comme lue
     */
    private void markAsRead(int notificationId) {
        sessionFacade.markNotificationAsRead(notificationId);
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Supprime une notification
     */
    private void deleteNotification(int notificationId) {
        sessionFacade.deleteNotification(notificationId);
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Marque toutes les notifications comme lues
     */
    @FXML
    private void onMarkAllAsRead() {
        sessionFacade.markAllMyNotificationsAsRead();
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Affiche toutes les notifications
     */
    @FXML
    private void onShowAll() {
        showOnlyUnread = false;
        updateFilterButtons();
        loadNotifications();
    }

    /**
     * Affiche uniquement les notifications non lues
     */
    @FXML
    private void onShowUnread() {
        showOnlyUnread = true;
        updateFilterButtons();
        loadNotifications();
    }

    /**
     * Met à jour le style des boutons de filtre
     */
    private void updateFilterButtons() {
        if (showOnlyUnread) {
            allFilterButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-padding: 8 16; -fx-background-radius: 8;");
            unreadFilterButton.setStyle("-fx-background-color: #356ee9; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8;");
        } else {
            allFilterButton.setStyle("-fx-background-color: #356ee9; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8;");
            unreadFilterButton.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-padding: 8 16; -fx-background-radius: 8;");
        }
    }

    /**
     * Met à jour le compteur de notifications non lues
     */
    private void updateUnreadCount() {
        int unreadCount = sessionFacade.getMyUnreadCount();
        unreadCountLabel.setText(unreadCount + (unreadCount <= 1 ? " non lue" : " non lues"));
        
        // Activer/désactiver le bouton "Tout marquer comme lu"
        markAllReadButton.setDisable(unreadCount == 0);
    }

    /**
     * Affiche l'état vide
     */
    private void showEmptyState() {
        notificationsContainer.setVisible(false);
        notificationsContainer.setManaged(false);
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    /**
     * Cache l'état vide
     */
    private void hideEmptyState() {
        notificationsContainer.setVisible(true);
        notificationsContainer.setManaged(true);
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
    }

    /**
     * Rafraîchit la vue (appelé depuis l'extérieur si nécessaire)
     */
    public void refresh() {
        loadNotifications();
        updateUnreadCount();
    }
}
