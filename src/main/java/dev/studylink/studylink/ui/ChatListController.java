package dev.studylink.studylink.ui;
import dev.studylink.studylink.ui.CreateChatDialogController;
import dev.studylink.studylink.business.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller pour la liste des chats
 * Affiche tous les chats de l'utilisateur
 */
public class ChatListController {

    @FXML
    private ListView<Chat> chatListView;

    @FXML
    private Label emptyStateLabel;

    @FXML
    private Button newChatButton;

    private SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;
    private BorderPane parentContentArea;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur courant
        currentUser = sessionFacade.getCurrentUser();

        if (currentUser == null) {
            emptyStateLabel.setText("Veuillez d'abord vous connecter");
            return;
        }

        // Configurer la ListView
        setupChatList();

        // Charger les chats
        loadChats();

        // Bouton pour créer un chat
        newChatButton.setOnAction(event -> createNewChat());
    }

    public void setParentContentArea(BorderPane contentArea) {
        this.parentContentArea = contentArea;
    }

    private void setupChatList() {
        // Custom cell pour afficher les chats
        chatListView.setCellFactory(param -> new ChatListCell());

        // Clique sur un chat pour l'ouvrir
        chatListView.setOnMouseClicked(event -> {
            Chat selectedChat = chatListView.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                openChat(selectedChat);
            }
        });
    }

    private void loadChats() {
        try {
            List<Chat> chats = sessionFacade.getChatsForUser(currentUser.getId());

            if (chats == null || chats.isEmpty()) {
                emptyStateLabel.setText("Aucun chat pour le moment");
                emptyStateLabel.setVisible(true);
                chatListView.setVisible(false);
            } else {
                emptyStateLabel.setVisible(false);
                chatListView.setVisible(true);
                chatListView.getItems().setAll(chats);
            }
        } catch (Exception e) {
            emptyStateLabel.setText("Erreur lors du chargement des chats: " + e.getMessage());
            emptyStateLabel.setVisible(true);
        }
    }

    private void openChat(Chat chat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/chat-view.fxml"));
            Parent root = loader.load();

            ChatViewController controller = loader.getController();
            controller.setChat(chat, currentUser);
            
            // Callback pour revenir à la liste des chats
            controller.setOnBackCallback(() -> {
                try {
                    FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/chat-list-view.fxml"));
                    Parent listRoot = listLoader.load();
                    
                    ChatListController listController = listLoader.getController();
                    listController.setParentContentArea(parentContentArea);
                    
                    if (parentContentArea != null) {
                        parentContentArea.setCenter(listRoot);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Si on a un parentContentArea, afficher dans l'app principale
            if (parentContentArea != null) {
                parentContentArea.setCenter(root);
            } else {
                // Fallback: créer une nouvelle fenêtre (ancienne méthode)
                Stage stage = new Stage();
                stage.setTitle("Chat - " + (chat.getName() != null ? chat.getName() : "Conversation"));
                stage.setScene(new Scene(root, 800, 600));
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du chat: " + e.getMessage());
        }
    }

    private void createNewChat() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/create-chat-dialog.fxml"));
            Parent root = loader.load();

            CreateChatDialogController controller = loader.getController();
            
            // Ajouter un callback pour revenir à la liste des chats après création/annulation
            controller.setOnBackCallback(() -> {
                try {
                    FXMLLoader listLoader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/chat-list-view.fxml"));
                    Parent listRoot = listLoader.load();
                    
                    ChatListController listController = listLoader.getController();
                    listController.setParentContentArea(parentContentArea);
                    
                    if (parentContentArea != null) {
                        parentContentArea.setCenter(listRoot);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            // Afficher dans la zone de contenu principale si disponible
            if (parentContentArea != null) {
                parentContentArea.setCenter(root);
            } else {
                // Fallback : créer une nouvelle fenêtre (ancien comportement)
                Stage stage = new Stage();
                stage.setTitle("Créer un nouveau chat");
                stage.setScene(new Scene(root, 400, 500));
                stage.showAndWait();

                // Récupérer le chat créé
                Chat createdChat = controller.getCreatedChat();
                if (createdChat != null) {
                    // Rafraîchir la liste des chats
                    refresh();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du dialog: " + e.getMessage());
        }
    }

    /**
     * Custom ListCell pour afficher les chats de manière élégante
     */
    private class ChatListCell extends ListCell<Chat> {
        private Label chatNameLabel;
        private Label participantCountLabel;
        private Label lastMessageTimeLabel;
        private HBox container;

        public ChatListCell() {
            chatNameLabel = new Label();
            chatNameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            participantCountLabel = new Label();
            participantCountLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

            lastMessageTimeLabel = new Label();
            lastMessageTimeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

            VBox textBox = new VBox(5);
            textBox.getChildren().addAll(
                    new HBox(10) {{
                        getChildren().addAll(chatNameLabel, lastMessageTimeLabel);
                        HBox.setHgrow(chatNameLabel, javafx.scene.layout.Priority.ALWAYS);
                    }},
                    participantCountLabel
            );

            container = new HBox(15);
            container.setPadding(new Insets(10));
            container.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
            container.getChildren().add(textBox);
            HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);
        }

        @Override
        protected void updateItem(Chat chat, boolean empty) {
            super.updateItem(chat, empty);

            if (empty || chat == null) {
                setGraphic(null);
            } else {
                // Pour les chats privés, afficher le nom du participant (pas le créateur)
                String chatTitle;
                if (chat.isPrivate()) {
                    // Trouver l'autre participant
                    User otherUser = null;
                    for (User participant : chat.getParticipants()) {
                        if (participant.getId() != currentUser.getId()) {
                            otherUser = participant;
                            break;
                        }
                    }
                    chatTitle = (otherUser != null) ? otherUser.getFullname() : "Chat privé";
                } else {
                    // Pour les groupes, afficher le nom du groupe
                    chatTitle = chat.getName() != null ? chat.getName() : "Chat groupe";
                }
                
                chatNameLabel.setText(chatTitle);

                // Afficher les participants du groupe
                String participantText;
                if (chat.isPrivate()) {
                    participantText = "Chat privé";
                } else {
                    StringBuilder participantNames = new StringBuilder();
                    for (int i = 0; i < chat.getParticipants().size(); i++) {
                        User p = chat.getParticipants().get(i);
                        participantNames.append(p.getFullname());
                        if (i < chat.getParticipants().size() - 1) {
                            participantNames.append(", ");
                        }
                    }
                    participantText = participantNames.toString();
                }
                participantCountLabel.setText(participantText);

                if (chat.getLastMessageAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    lastMessageTimeLabel.setText(chat.getLastMessageAt().format(formatter));
                } else {
                    lastMessageTimeLabel.setText("Nouveau");
                }

                setGraphic(container);
            }
        }
    }

    /**
     * Rafraîchir la liste des chats (appelé depuis l'extérieur)
     */
    public void refresh() {
        loadChats();
    }
}