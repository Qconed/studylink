package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller pour l'affichage d'un chat
 * Affiche les messages et permet d'en envoyer
 */
public class ChatViewController {

    @FXML
    private Label chatTitleLabel;

    @FXML
    private ListView<Message> messageListView;

    @FXML
    private TextArea messageInputArea;

    @FXML
    private Button sendButton;

    @FXML
    private Label participantCountLabel;

    @FXML
    private Button backButton;

    private SessionFacade sessionFacade = SessionFacade.getInstance();
    private Chat currentChat;
    private User currentUser;
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    private Runnable onBackCallback;

    @FXML
    public void initialize() {
        // Configurer la ListView des messages
        setupMessageList();

        // Bouton d'envoi
        sendButton.setOnAction(event -> sendMessage());
        
        // Bouton retour (si disponible)
        if (backButton != null) {
            backButton.setOnAction(event -> goBack());
        }

        // Entrer pour envoyer (Ctrl+Enter)
        messageInputArea.setWrapText(true);
    }

    /**
     * Définir le chat à afficher
     */
    public void setChat(Chat chat, User user) {
        this.currentChat = chat;
        this.currentUser = user;

        loadChatData();
    }

    /**
     * Définir le callback appelé quand l'utilisateur clique sur retour
     */
    public void setOnBackCallback(Runnable callback) {
        this.onBackCallback = callback;
    }

    private void goBack() {
        if (onBackCallback != null) {
            onBackCallback.run();
        }
    }

    private void setupMessageList() {
        // Custom cell pour afficher les messages
        messageListView.setCellFactory(param -> new MessageListCell(currentUser));
        messageListView.setItems(messages);
    }

    private void loadChatData() {
        try {
            // Titre du chat
            String chatTitle = currentChat.getName() != null ?
                    currentChat.getName() :
                    (currentChat.isPrivate() ? "Chat privé" : "Chat groupe");
            chatTitleLabel.setText(chatTitle);

            // Nombre de participants
            participantCountLabel.setText(
                    currentChat.getParticipants().size() + " participant(s)"
            );

            // Charger les messages (page 1, 50 messages)
            List<Message> messageList = sessionFacade.getMessages(currentChat.getId(), 1);
            if (messageList != null) {
                messages.setAll(messageList);
                // Scroller vers le bas
                messageListView.scrollTo(messages.size() - 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du chat: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String content = messageInputArea.getText().trim();

        if (content.isEmpty()) {
            showError("Le message ne peut pas être vide");
            return;
        }

        if (content.length() > 5000) {
            showError("Le message est trop long (max 5000 caractères)");
            return;
        }

        try {
            Message message = sessionFacade.sendMessage(
                    currentUser.getId(),
                    currentChat.getId(),
                    content
            );

            if (message != null) {
                messages.add(message);
                messageInputArea.clear();

                // Scroller vers le bas
                messageListView.scrollTo(messages.size() - 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Custom ListCell pour afficher les messages de manière élégante
     */
    private class MessageListCell extends ListCell<Message> {
        private Label senderNameLabel;
        private Label messageContentLabel;
        private Label timeLabel;
        private HBox messageBox;
        private VBox contentBox;
        private User currentUser;

        public MessageListCell(User currentUser) {
            this.currentUser = currentUser;

            senderNameLabel = new Label();
            senderNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

            messageContentLabel = new Label();
            messageContentLabel.setStyle("-fx-font-size: 13; -fx-wrap-text: true;");
            messageContentLabel.setWrapText(true);

            timeLabel = new Label();
            timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

            contentBox = new VBox(5);
            contentBox.getChildren().addAll(
                    new HBox(10) {{
                        getChildren().addAll(senderNameLabel, timeLabel);
                        HBox.setHgrow(senderNameLabel, javafx.scene.layout.Priority.ALWAYS);
                    }},
                    messageContentLabel
            );
            contentBox.setPadding(new Insets(8, 12, 8, 12));

            messageBox = new HBox();
            messageBox.setPadding(new Insets(5, 10, 5, 10));
            messageBox.setSpacing(10);
        }

        @Override
        protected void updateItem(Message message, boolean empty) {
            super.updateItem(message, empty);

            if (empty || message == null) {
                setGraphic(null);
            } else {
                // Déterminer si c'est le message de l'utilisateur courant
                boolean isCurrentUser = message.getSenderId() == currentUser.getId();

                String senderName;
                if (message.getSenderId() == currentUser.getId()) {
                    senderName = "Vous";
                } else {
                    senderName = message.getSenderFullname() != null && !message.getSenderFullname().isEmpty()
                            ? message.getSenderFullname()
                            : "Utilisateur #" + message.getSenderId();
                }
                senderNameLabel.setText(senderName);

                messageContentLabel.setText(message.getContent());

                if (message.getCreatedAt() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    timeLabel.setText(message.getCreatedAt().format(formatter));
                }

                // Styling selon que c'est l'utilisateur ou non
                if (isCurrentUser) {
                    contentBox.setStyle(
                            "-fx-background-color: #007bff; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-border-radius: 10;"
                    );
                    senderNameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                    timeLabel.setTextFill(javafx.scene.paint.Color.web("#e0e0e0"));
                    messageContentLabel.setTextFill(javafx.scene.paint.Color.WHITE);

                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    contentBox.setStyle(
                            "-fx-background-color: #e9ecef; " +
                                    "-fx-background-radius: 10; " +
                                    "-fx-border-radius: 10;"
                    );
                    messageBox.setAlignment(Pos.CENTER_LEFT);
                }

                messageBox.getChildren().clear();
                messageBox.getChildren().add(contentBox);
                HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

                setGraphic(messageBox);
            }
        }
    }
}