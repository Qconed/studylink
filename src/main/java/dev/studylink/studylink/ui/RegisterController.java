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

public class RegisterController {
    //TODO : enlever firstname te lastname et remplacer par fullname
    @FXML
    private TextField lastnameField;

    @FXML
    private TextField firstnameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel; // Pense à ajouter ce Label dans ton FXML pour afficher les erreurs (en rouge)

    // Instance de la façade pour communiquer avec la couche Business
    private final SessionFacade sessionFacade = SessionFacade.getInstance();

    @FXML
    protected void onRegisterButtonClick() {
        // 1. Récupération des données
        //TODO : la meme chose que Fullname
        String nom = lastnameField.getText();
        String prenom = firstnameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 2. Validation simple
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            errorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            errorLabel.setVisible(true);
            return;
        }

        // 3. Appel au métier pour créer l'utilisateur
        // (Tu devras créer cette méthode 'register' dans SessionFacade juste après)
        // TODO : que fullname
        boolean isRegistered = sessionFacade.register(nom, prenom, email, password);

        if (isRegistered) {
            // Inscription réussie : on redirige vers le Login ou l'Accueil
            System.out.println("Inscription réussie pour " + email);
            loadLoginView();
        } else {
            errorLabel.setText("Erreur : Cet email est peut-être déjà utilisé.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    protected void onBackToLoginClick() {
        loadLoginView();
    }

    // Méthode utilitaire pour charger la vue de connexion
    private void loadLoginView() {
        try {
            // Attention au chemin : adapte-le si ton login-view.fxml est ailleurs
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Récupère la fenêtre actuelle (Stage) via n'importe quel élément (ici le champ email)
            // Si le champ email n'est pas encore initialisé (cas rare), utilise une autre méthode,
            // mais ici c'est sûr car on clique sur un bouton.
            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setTitle("Connexion - StudyLink");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page de connexion.");
            errorLabel.setVisible(true);
        }
    }
}