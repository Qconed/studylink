package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.UserAlreadyExists;
import dev.studylink.studylink.exception.UserDoesNotExist;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ProfileController {
    @FXML
    private TextField fullnameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea bioField;

    @FXML
    private FlowPane categoriesFlowPane;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        loadUserProfile();
        loadAvailableCategories();
    }

    private void loadUserProfile() {
        currentUser = sessionFacade.getCurrentUser();
        if (currentUser != null) {
            fullnameField.setText(currentUser.getFullname());
            emailField.setText(currentUser.getEmail());
            bioField.setText(currentUser.getBio() != null ? currentUser.getBio() : "");
            displayUserCategories();
        }
    }

    private void loadAvailableCategories() {
        List<Category> allCategories = sessionFacade.getAllCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(allCategories);
        categoryComboBox.setItems(categoryList);
    }

    private void displayUserCategories() {
        categoriesFlowPane.getChildren().clear();

        if (currentUser != null) {
            for (Category category : currentUser.getCategories()) {
                Button categoryButton = new Button(category.getTitle());
                categoryButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; " +
                        "-fx-padding: 5 10; -fx-background-radius: 15;");

                // Add remove functionality
                categoryButton.setOnAction(e -> {
                    if (sessionFacade.removeCategoryFromUser(currentUser.getId(), category.getId())) {
                        currentUser.removeCategory(category.getId());
                        displayUserCategories();
                        showSuccess("Catégorie supprimée");
                    }
                });

                categoriesFlowPane.getChildren().add(categoryButton);
            }
        }
    }

    @FXML
    protected void onSaveProfileClick() {
        String fullname = fullnameField.getText();
        String email = emailField.getText();
        String bio = bioField.getText();

        if (fullname.isEmpty() || email.isEmpty()) {
            showError("Le nom et l'email sont requis");
            return;
        }

        try {
            // Update email if changed
            if (!email.equals(currentUser.getEmail())) {
                sessionFacade.updateEmail(currentUser.getId(), email);
                currentUser.setEmail(email);
            }

            // Update profile
            if (sessionFacade.updateProfile(currentUser.getId(), fullname, bio)) {
                currentUser.setFullname(fullname);
                currentUser.setBio(bio);
                showSuccess("Profil mis à jour avec succès!");
            } else {
                showError("Erreur lors de la mise à jour");
            }
        } catch (UserAlreadyExists e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onAddCategoryClick() {
        Category selectedCategory = categoryComboBox.getValue();

        if (selectedCategory == null) {
            showError("Veuillez sélectionner une catégorie");
            return;
        }

        if (currentUser.hasCategory(selectedCategory.getId())) {
            showError("Cette catégorie est déjà ajoutée");
            return;
        }

        if (sessionFacade.addCategoryToUser(currentUser.getId(), selectedCategory)) {
            currentUser.addCategory(selectedCategory);
            displayUserCategories();
            showSuccess("Catégorie ajoutée");
            categoryComboBox.setValue(null);
        } else {
            showError("Erreur lors de l'ajout de la catégorie");
        }
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/main-app-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) fullnameField.getScene().getWindow();
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