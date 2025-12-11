package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.SessionFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // Instance de ta façade métier
    private final SessionFacade sessionFacade = new SessionFacade();

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Appel à ta logique métier (à adapter selon ta méthode réelle dans SessionFacade)
        boolean isAuthenticated = sessionFacade.login(email, password);

        if (isAuthenticated) {
            errorLabel.setVisible(false);
            System.out.println("Connexion réussie !");
            // TODO: Changer de scène ici pour aller vers l'accueil
            // loadMainView();
        } else {
            errorLabel.setText("Identifiants incorrects.");
            errorLabel.setVisible(true);
        }
    }
}