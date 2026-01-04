package dev.studylink.studylink.ui;

import java.io.IOException;

import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserDoesNotExist;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        try {
            User user = sessionFacade.login(password, email);
            errorLabel.setVisible(false);

            // Redirect to home page
            loadHomeView();

        } catch (LoginError | UserDoesNotExist e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/register-view.fxml"));
            Scene registerScene = new Scene(fxmlLoader.load());
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.setTitle("Inscription - StudyLink");
            currentStage.setScene(registerScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page d'inscription");
            errorLabel.setVisible(true);
        }
    }

    private void loadHomeView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/main-app-view.fxml"));
            Scene homeScene = new Scene(fxmlLoader.load());
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.setTitle("StudyLink");
            currentStage.setMinWidth(1000);
            currentStage.setMinHeight(700);
            currentStage.setWidth(1200);
            currentStage.setHeight(800);
            currentStage.setResizable(true);
            currentStage.setScene(homeScene);
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement de la page d'accueil");
            errorLabel.setVisible(true);
        }
    }
}