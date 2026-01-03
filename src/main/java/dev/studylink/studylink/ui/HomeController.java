package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Button adminPanelButton;

    @FXML
    private Label errorLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getFullname() + "!");
            roleLabel.setText("Rôle: " + currentUser.getRole().toString());

            // Show admin panel button only for admins
            adminPanelButton.setVisible(currentUser.getRole() == Role.ADMIN);
            adminPanelButton.setManaged(currentUser.getRole() == Role.ADMIN);
        } else {
            welcomeLabel.setText("Aucun utilisateur connecté");
            adminPanelButton.setVisible(false);
        }
    }

    @FXML
    protected void onMyProfileClick() {
        loadView("/dev/studylink/studylink/profile-view.fxml", "Mon Profil");
    }

    @FXML
    protected void onFriendsClick() {
        loadView("/dev/studylink/studylink/friends-view.fxml", "Mes Amis");
    }

    @FXML
    protected void onAdminPanelClick() {
        if (currentUser.getRole() == Role.ADMIN) {
            loadView("/dev/studylink/studylink/admin-panel-view.fxml", "Panneau d'Administration");
        } else {
            showError("Accès refusé");
        }
    }

    @FXML
    protected void onLogoutClick() {
        sessionFacade.logout();
        loadView("/dev/studylink/studylink/login-view.fxml", "Connexion - StudyLink");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}