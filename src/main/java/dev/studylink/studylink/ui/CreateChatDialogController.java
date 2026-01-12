package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;

import java.util.List;
import java.util.Optional;

/**
 * Controller pour créer un nouveau chat
 */
public class CreateChatDialogController {

    @FXML
    private RadioButton privateRadio;

    @FXML
    private RadioButton groupRadio;

    @FXML
    private Label selectUserLabel;

    @FXML
    private TextField userSearchField;

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private Label groupNameLabel;

    @FXML
    private TextField groupNameField;

    @FXML
    private ListView<User> userListView;

    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;
    private Chat createdChat;
    private List<User> allUsers;
    private Runnable onBackCallback;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        // Radio buttons
        ToggleGroup group = new ToggleGroup();
        privateRadio.setToggleGroup(group);
        groupRadio.setToggleGroup(group);
        privateRadio.setSelected(true);

        // Listeners
        privateRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showPrivateChat();
            }
        });

        groupRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showGroupChat();
            }
        });

        // Load users for combo box
        loadUsers();

        // Search field listener
        if (userSearchField != null) {
            userSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterUsersByFullname(newVal);
            });
        }

        // Buttons
        createButton.setOnAction(event -> createNewChat());
        cancelButton.setOnAction(event -> closeDialog());

        // Initial state
        showPrivateChat();
    }

    /**
     * Définir le callback appelé quand l'utilisateur clique sur annuler ou revient
     */
    public void setOnBackCallback(Runnable callback) {
        this.onBackCallback = callback;
    }

    private void showPrivateChat() {
        selectUserLabel.setVisible(true);
        userComboBox.setVisible(true);

        groupNameLabel.setVisible(false);
        groupNameField.setVisible(false);
        userListView.setVisible(false);
    }

    private void showGroupChat() {
        selectUserLabel.setVisible(false);
        userComboBox.setVisible(false);

        groupNameLabel.setVisible(true);
        groupNameField.setVisible(true);
        userListView.setVisible(true);
    }

    private void loadUsers() {
        try {
            // Récupérer tous les utilisateurs sans vérification de droits
            List<User> allUsersFromFacade = sessionFacade.getAllUsersPublic();
            this.allUsers = allUsersFromFacade;
            ObservableList<User> users = FXCollections.observableArrayList();

            for (User user : allUsersFromFacade) {
                if (user.getId() != currentUser.getId()) {
                    users.add(user);
                }
            }

            // Configurer le ComboBox pour afficher uniquement le fullName
            userComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getFullname() : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Configurer le CellFactory pour la ListView
            userListView.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getFullname());
                    }
                }
            });

            userComboBox.setItems(users);
            userListView.setItems(users);

        } catch (Exception e) {
            showError("Erreur lors du chargement des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterUsersByFullname(String searchText) {
        if (allUsers == null || allUsers.isEmpty()) {
            return;
        }

        ObservableList<User> filteredUsers = FXCollections.observableArrayList();
        String searchLower = searchText.toLowerCase().trim();

        for (User user : allUsers) {
            if (user.getId() != currentUser.getId() &&
                user.getFullname().toLowerCase().contains(searchLower)) {
                filteredUsers.add(user);
            }
        }

        if (privateRadio.isSelected()) {
            userComboBox.setItems(filteredUsers);
        } else {
            userListView.setItems(filteredUsers);
        }
    }

    private void createNewChat() {
        try {
            if (privateRadio.isSelected()) {
                createPrivateChat();
            } else {
                createGroupChat();
            }
        } catch (Exception e) {
            showError("Erreur lors de la création du chat: " + e.getMessage());
        }
    }

    private void createPrivateChat() throws Exception {
        User selectedUser = userComboBox.getValue();

        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur");
            return;
        }

        createdChat = sessionFacade.createPrivateChat(currentUser, selectedUser);

        if (createdChat != null) {
            showInfo("Chat privé créé avec succès!");
            closeDialog();
        } else {
            showError("Impossible de créer le chat");
        }
    }

    private void createGroupChat() throws Exception {
        String groupName = groupNameField.getText().trim();

        if (groupName.isEmpty()) {
            showError("Veuillez entrer un nom pour le groupe");
            return;
        }

        ObservableList<User> selectedUsers = userListView.getSelectionModel().getSelectedItems();

        if (selectedUsers.isEmpty()) {
            showError("Veuillez sélectionner au moins un utilisateur");
            return;
        }

        createdChat = sessionFacade.createGroupChat(groupName, currentUser, (List<User>) selectedUsers);

        if (createdChat != null) {
            showInfo("Chat groupe créé avec succès!");
            closeDialog();
        } else {
            showError("Impossible de créer le chat");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDialog() {
        if (onBackCallback != null) {
            onBackCallback.run();
        } else {
            // Fallback pour les anciennes fenêtres
            try {
                if (cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
                    cancelButton.getScene().getWindow().hide();
                }
            } catch (Exception e) {
                // Ignore if window is not available
            }
        }
    }

    public Chat getCreatedChat() {
        return createdChat;
    }
}