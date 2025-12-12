package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.SessionFacade;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Alert;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // Instance de ta façade métier
    private final SessionFacade sessionFacade = SessionFacade.getInstance();

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Appel à ta logique métier (à adapter selon ta méthode réelle dans SessionFacade)
        boolean isAuthenticated = sessionFacade.login(email, password);

        if (isAuthenticated) {
            errorLabel.setVisible(false);

            // --- DÉBUT DU MESSAGE DE SUCCÈS ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Connexion réussie");
            alert.setHeaderText(null); // Met à null pour éviter un en-tête redondant
            alert.setContentText("Bienvenue sur StudyLink ! Vous êtes connecté.");

            // Affiche l'alerte et attend que l'utilisateur clique sur "OK"
            alert.showAndWait();
            // --- FIN ---

            // Une fois qu'il a cliqué sur OK, tu peux changer de scène
            System.out.println("Transition vers l'accueil...");
            // loadMainView();
        } else {
            errorLabel.setText("Identifiants incorrects.");
            errorLabel.setVisible(true);
        }
    }
    @FXML
    protected void onRegisterButtonClick() {
        try {
            // 1. Charger le fichier FXML de l'inscription
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/register-view.fxml"));
            // Attention : vérifie bien le chemin de ton fichier fxml !
            // Si tes fxml sont tous au même endroit, getClass().getResource("register-view.fxml") suffit souvent.

            Scene registerScene = new Scene(fxmlLoader.load());

            // 2. Récupérer la fenêtre actuelle (Stage) à partir d'un élément de la scène (ex: le champ email)
            // Note: Tu peux utiliser n'importe quel @FXML injecté (emailField, loginButton, etc.)
            Stage currentStage = (Stage) emailField.getScene().getWindow();

            // 3. Changer la scène
            currentStage.setTitle("Inscription - StudyLink");
            currentStage.setScene(registerScene);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Affiche l'erreur si le fichier FXML est introuvable
        }
    }

}