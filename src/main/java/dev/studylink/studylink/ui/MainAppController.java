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
    private Button homeButton;

    @FXML
    private BorderPane contentArea;

    @FXML
    private Button profileButton;

    @FXML
    private Button friendsButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button categoriesButton;

    @FXML
    private Button resourcesButton;
    
    @FXML
    private Button cartButton;

    @FXML
    private Button logoutButton;



    private void loadDashboard() {
        loadContent("/dev/studylink/studylink/dashboard-content.fxml");
    }

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    // Instance statique pour permettre aux autres contrôleurs de naviguer
    private static MainAppController instance;

    @FXML
    public void initialize() {
        instance = this;

        // Load CSS stylesheet for the entire scene
        try {
            String css = getClass().getResource("/dev/studylink/studylink/styles.css").toExternalForm();
            contentArea.getStylesheets().add(css);
            sidebarMenu.getParent().getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS: " + e.getMessage());
        }

        currentUser = sessionFacade.getCurrentUser();

        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullname());
            userRoleLabel.setText(currentUser.getRole().toString());

            boolean isAdmin = currentUser.getRole() == Role.ADMIN;
            usersButton.setVisible(isAdmin);
            usersButton.setManaged(isAdmin);
            categoriesButton.setVisible(isAdmin);
            categoriesButton.setManaged(isAdmin);

            // CHARGEMENT DU DASHBOARD PAR DÉFAUT
            onHomeClick();
        }
    }

    // Méthode statique pour permettre aux autres contrôleurs de charger du contenu
    public static void loadContentStatic(String fxmlPath) {
        if (instance != null) {
            instance.loadContent(fxmlPath);
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
    protected void onHomeClick() {
        // Charge le fichier FXML du dashboard dans la zone centrale
        loadContent("/dev/studylink/studylink/dashboard-content.fxml");
        
        // Met le bouton en surbrillance (bleu)
        setActiveButton(homeButton);
    }

    @FXML
    protected void onUsersClick() {
        loadContent("/dev/studylink/studylink/admin-users-content.fxml");
        setActiveButton(usersButton);
    }

    @FXML
    protected void onCategoriesClick() {
        loadContent("/dev/studylink/studylink/admin-categories-content.fxml");
        setActiveButton(categoriesButton);
    }

    @FXML
    protected void onResourcesClick() {
        loadContent("/dev/studylink/studylink/resource-feed-view.fxml");
        setActiveButton(resourcesButton);
    }
    
    @FXML
    protected void onCartClick() {
        loadContent("/dev/studylink/studylink/cart-view.fxml");
        setActiveButton(cartButton);
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

    // Méthode publique pour permettre aux autres contrôleurs de charger du contenu
    public void loadContent(String fxmlPath) {
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
        homeButton.getStyleClass().remove("active-menu-button");
        profileButton.getStyleClass().remove("active-menu-button");
        friendsButton.getStyleClass().remove("active-menu-button");
        usersButton.getStyleClass().remove("active-menu-button");
        categoriesButton.getStyleClass().remove("active-menu-button");
        resourcesButton.getStyleClass().remove("active-menu-button");
        cartButton.getStyleClass().remove("active-menu-button");

        // Set active button
        if (activeButton != null) {
            activeButton.getStyleClass().add("active-menu-button");
        }
    }

    public static MainAppController getInstance() {
        return instance;
    }
}