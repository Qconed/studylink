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
    
    @FXML
    private Button addMemberButton;
    
    @FXML
    private Label participantListLabel;

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
        
        // Bouton ajouter membre (pour groupes uniquement)
        if (addMemberButton != null) {
            addMemberButton.setOnAction(event -> addMember());
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

            // Nombre de participants et liste
            participantCountLabel.setText(
                    currentChat.getParticipants().size() + " participant(s)"
            );
            
            // Afficher la liste des participants
            StringBuilder participantNames = new StringBuilder();
            for (int i = 0; i < currentChat.getParticipants().size(); i++) {
                User p = currentChat.getParticipants().get(i);
                participantNames.append(p.getFullname());
                if (i < currentChat.getParticipants().size() - 1) {
                    participantNames.append(", ");
                }
            }
            if (participantListLabel != null) {
                participantListLabel.setText("Participants: " + participantNames.toString());
            }
            
            // Afficher le bouton ajouter membre uniquement pour les groupes
            if (addMemberButton != null) {
                addMemberButton.setVisible(!currentChat.isPrivate());
                addMemberButton.setManaged(!currentChat.isPrivate());
            }

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
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void addMember() {
        // Créer une dialog pour sélectionner un utilisateur
        try {
            List<User> allUsers = sessionFacade.getAllUsersPublic();
            
            // Filtrer les utilisateurs déjà dans le chat
            ObservableList<User> availableUsers = FXCollections.observableArrayList();
            for (User user : allUsers) {
                boolean isAlreadyMember = currentChat.getParticipants().stream()
                        .anyMatch(p -> p.getId() == user.getId());
                if (!isAlreadyMember) {
                    availableUsers.add(user);
                }
            }
            
            if (availableUsers.isEmpty()) {
                showError("Tous les utilisateurs sont déjà dans le groupe");
                return;
            }
            
            // Dialog pour sélectionner l'utilisateur
            ChoiceDialog<User> dialog = new ChoiceDialog<>(
                    availableUsers.get(0),
                    availableUsers
            );
            dialog.setTitle("Ajouter un membre");
            dialog.setHeaderText("Sélectionnez un utilisateur à ajouter au groupe");
            dialog.setContentText("Utilisateur:");
            
            // Afficher seulement le fullname dans le ComboBox
            @SuppressWarnings("unchecked")
            ComboBox<User> comboBox = (ComboBox<User>) dialog.getDialogPane().lookup(".combo-box-base");
            if (comboBox != null) {
                comboBox.setCellFactory(param -> new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getFullname());
                    }
                });
                comboBox.setButtonCell(new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getFullname());
                    }
                });
            }
            
            var result = dialog.showAndWait();
            if (result.isPresent()) {
                User selectedUser = result.get();
                
                // Ajouter le participant
                boolean success = sessionFacade.addParticipantToGroup(currentChat.getId(), selectedUser);
                if (success) {
                    // Ajouter un message système
                    String systemMessage = selectedUser.getFullname() + " a rejoint le chat";
                    sessionFacade.sendMessage(currentUser.getId(), currentChat.getId(), systemMessage);
                    
                    // Recharger les données
                    currentChat = sessionFacade.getChatById(currentChat.getId()).orElse(currentChat);
                    loadChatData();
                    showSuccess("Participant ajouté avec succès");
                } else {
                    showError("Erreur lors de l'ajout du participant");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    timeLabel.setText(message.getCreatedAt().format(formatter));
                }

                // Styling selon que c'est l'utilisateur ou non
                if (isCurrentUser) {
                    contentBox.setStyle(
                            "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                                    "-fx-text-fill: white; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-border-radius: 12; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.25), 4, 0, 0, 1);"
                    );
                    senderNameLabel.setTextFill(javafx.scene.paint.Color.web("#ffffff"));
                    timeLabel.setTextFill(javafx.scene.paint.Color.web("#e8d5ff"));
                    messageContentLabel.setTextFill(javafx.scene.paint.Color.WHITE);

                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    contentBox.setStyle(
                            "-fx-background-color: #ffffff; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-border-radius: 12; " +
                                    "-fx-border-color: #e8e8e8; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 1);"
                    );
                    senderNameLabel.setTextFill(javafx.scene.paint.Color.web("#667eea"));
                    timeLabel.setTextFill(javafx.scene.paint.Color.web("#999999"));
                    messageContentLabel.setTextFill(javafx.scene.paint.Color.web("#333333"));
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