package dev.studylink.studylink.ui;

import java.io.IOException;

import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainAppController {
    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private VBox sidebarMenu;

    @FXML
    private BorderPane contentArea;

    @FXML
    private Button profileButton;

    @FXML
    private Button friendsButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button logoutButton;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        // Load CSS stylesheet
        try {
            String css = getClass().getResource("/dev/studylink/studylink/styles.css").toExternalForm();
            contentArea.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS: " + e.getMessage());
        }

        currentUser = sessionFacade.getCurrentUser();

        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullname());
            userRoleLabel.setText(currentUser.getRole().toString());

            // Show/hide admin buttons
            boolean isAdmin = currentUser.getRole() == Role.ADMIN;
            usersButton.setVisible(isAdmin);
            usersButton.setManaged(isAdmin);

            // Load default view (profile)
            loadProfile();
        }
    }

    @FXML
    protected void onProfileClick() {
        loadProfile();
        setActiveButton(profileButton);
    }

    @FXML
    protected void onFriendsClick() {
        loadContent("/dev/studylink/studylink/friends-content.fxml");
        setActiveButton(friendsButton);
    }

    @FXML
    protected void onUsersClick() {
        loadContent("/dev/studylink/studylink/admin-users-content.fxml");
        setActiveButton(usersButton);
    }

    @FXML
    protected void onLogoutClick() {
        sessionFacade.logout();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("StudyLink - Connexion");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        loadContent("/dev/studylink/studylink/profile-content.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            contentArea.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de: " + fxmlPath);
        }
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons
        profileButton.getStyleClass().remove("active-menu-button");
        friendsButton.getStyleClass().remove("active-menu-button");
        usersButton.getStyleClass().remove("active-menu-button");

        // Set active button
        activeButton.getStyleClass().add("active-menu-button");
    }
}