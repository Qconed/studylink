package dev.studylink.studylink.ui;

import java.io.IOException;
import java.util.List;

import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.UnauthorizedException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AdminPanelController {
    @FXML
    private ListView<User> usersListView;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private TextArea suspensionReasonArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User adminUser;
    private User selectedUser;

    @FXML
    public void initialize() {
        adminUser = sessionFacade.getCurrentUser();

        // Check if user is admin
        if (adminUser == null || adminUser.getRole() != Role.ADMIN) {
            showError("Accès refusé: droits administrateur requis");
            return;
        }

        // Setup role combo box
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));

        // Setup users list view
        setupUsersListView();

        loadAllUsers();
    }

    private void setupUsersListView() {
        usersListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(5));

                Label nameLabel = new Label(user.getFullname());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #D3D2D7; -fx-min-width: 150;");

                Label emailLabel = new Label(user.getEmail());
                emailLabel.setStyle("-fx-text-fill: #888888; -fx-min-width: 200;");

                Label roleLabel = new Label(user.getRole().toString());
                roleLabel.setStyle("-fx-text-fill: #4618F4; -fx-font-weight: bold; -fx-min-width: 80;");

                Label statusLabel = new Label(user.isSuspended() ? "SUSPENDU" : "ACTIF");
                statusLabel.setStyle(user.isSuspended() ?
                        "-fx-text-fill: #F44336; -fx-font-weight: bold;" :
                        "-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                Button selectButton = new Button("Sélectionner");
                selectButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white;");
                selectButton.setOnAction(e -> onSelectUser(user));

                hbox.getChildren().addAll(nameLabel, emailLabel, roleLabel, statusLabel, selectButton);
                setGraphic(hbox);
            }
        });
    }

    private void onSelectUser(User user) {
        selectedUser = user;
        roleComboBox.setValue(user.getRole());
        suspensionReasonArea.setText(user.getSuspensionReason() != null ? user.getSuspensionReason() : "");
        showSuccess("Utilisateur sélectionné: " + user.getFullname());
    }

    @FXML
    protected void onChangeRoleClick() {
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur");
            return;
        }

        Role newRole = roleComboBox.getValue();
        if (newRole == null) {
            showError("Veuillez sélectionner un rôle");
            return;
        }

        if (selectedUser.getId() == adminUser.getId()) {
            showError("Vous ne pouvez pas modifier votre propre rôle");
            return;
        }

        try {
            if (sessionFacade.changeUserRole(adminUser.getId(), selectedUser.getId(), newRole)) {
                showSuccess("Rôle modifié avec succès");
                refreshUsersList();
            } else {
                showError("Erreur lors de la modification du rôle");
            }
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onSuspendUserClick() {
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur");
            return;
        }

        String reason = suspensionReasonArea.getText().trim();
        if (reason.isEmpty()) {
            showError("Veuillez entrer une raison de suspension");
            return;
        }

        if (selectedUser.getId() == adminUser.getId()) {
            showError("Vous ne pouvez pas vous suspendre vous-même");
            return;
        }

        try {
            if (sessionFacade.suspendUser(adminUser.getId(), selectedUser.getId(), reason)) {
                showSuccess("Utilisateur suspendu");
                refreshUsersList();
            } else {
                showError("Erreur lors de la suspension");
            }
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onUnsuspendUserClick() {
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur");
            return;
        }

        if (!selectedUser.isSuspended()) {
            showError("Cet utilisateur n'est pas suspendu");
            return;
        }

        try {
            if (sessionFacade.unsuspendUser(adminUser.getId(), selectedUser.getId())) {
                showSuccess("Utilisateur réactivé");
                refreshUsersList();
            } else {
                showError("Erreur lors de la réactivation");
            }
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onDeleteUserClick() {
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur");
            return;
        }

        if (selectedUser.getId() == adminUser.getId()) {
            showError("Vous ne pouvez pas vous supprimer vous-même");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer " + selectedUser.getFullname() + "?");
        confirmAlert.setContentText("Cette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (sessionFacade.deleteUser(adminUser.getId(), selectedUser.getId())) {
                        showSuccess("Utilisateur supprimé");
                        selectedUser = null;
                        refreshUsersList();
                    } else {
                        showError("Erreur lors de la suppression");
                    }
                } catch (UnauthorizedException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private void loadAllUsers() {
        try {
            List<User> users = sessionFacade.getAllUsers(adminUser.getId());
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            usersListView.setItems(userList);
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
            e.printStackTrace();
        } 
    }

    private void refreshUsersList() {
        loadAllUsers();
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/main-app-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) usersListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'accueil");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}