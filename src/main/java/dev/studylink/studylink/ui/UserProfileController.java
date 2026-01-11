package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import dev.studylink.studylink.exception.UserDoesNotExist;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class UserProfileController {
    @FXML private Label fullnameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label bioLabel;
    @FXML private FlowPane categoriesFlowPane;
    @FXML private ListView<Resource> resourcesListView;
    @FXML private Button friendRequestButton;
    @FXML private Button alreadyFriendsButton;
    @FXML private Button pendingRequestButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private User currentUser;
    private User viewedUser;

    private static User userToLoad;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        if (userToLoad != null) {
            setUser(userToLoad);
            userToLoad = null;
        }

        setupResourcesListView();
    }

    public static void setUserToLoad(User user) {
        userToLoad = user;
    }

    public void setUser(User user) {
        this.viewedUser = user;
        displayUserInfo();
        displayCategories();
        loadUserResources();
        updateFriendshipStatus();
    }

    private void displayUserInfo() {
        if (viewedUser == null) return;

        fullnameLabel.setText(viewedUser.getFullname());
        emailLabel.setText(viewedUser.getEmail());
        roleLabel.setText(viewedUser.getRole().toString());

        String bio = viewedUser.getBio();
        if (bio != null && !bio.trim().isEmpty()) {
            bioLabel.setText(bio);
        } else {
            bioLabel.setText("Aucune biographie disponible.");
            bioLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
        }
    }

    private void displayCategories() {
        categoriesFlowPane.getChildren().clear();

        List<Category> categories = viewedUser.getCategories();

        if (categories == null || categories.isEmpty()) {
            Label noCategories = new Label("Aucune catégorie");
            noCategories.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            categoriesFlowPane.getChildren().add(noCategories);
        } else {
            for (Category category : categories) {
                Label catLabel = new Label(category.getTitle());
                catLabel.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; " +
                        "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 13px;");
                categoriesFlowPane.getChildren().add(catLabel);
            }
        }
    }

    private void setupResourcesListView() {
        resourcesListView.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);

                if (empty || resource == null) {
                    setGraphic(null);
                    return;
                }

                VBox vbox = new VBox(10);
                vbox.setPadding(new Insets(10));
                vbox.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");

                Label titleLabel = new Label(resource.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A1640;");
                titleLabel.setWrapText(true);

                Label statsLabel = new Label("👁 " + resource.getViewCount() + " vues  |  ⭐ " +
                        resource.getSaveCount() + " sauvegardes");
                statsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

                Button viewButton = new Button("Voir détails");
                viewButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; " +
                        "-fx-font-weight: bold;");
                viewButton.setOnAction(e -> onViewResourceClick(resource));

                vbox.getChildren().addAll(titleLabel, statsLabel, viewButton);
                setGraphic(vbox);
            }
        });
    }

    private void loadUserResources() {
        List<Resource> resources = resourceFacade.getResourcesByOwner(viewedUser.getId());
        resourcesListView.setItems(FXCollections.observableArrayList(resources));

        if (resources.isEmpty()) {
            Label placeholder = new Label("Aucune ressource partagée");
            placeholder.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            resourcesListView.setPlaceholder(placeholder);
        }
    }

    private void updateFriendshipStatus() {
        // Hide all buttons first
        friendRequestButton.setVisible(false);
        friendRequestButton.setManaged(false);
        alreadyFriendsButton.setVisible(false);
        alreadyFriendsButton.setManaged(false);
        pendingRequestButton.setVisible(false);
        pendingRequestButton.setManaged(false);

        // Don't show friendship buttons for own profile
        if (viewedUser.getId() == currentUser.getId()) {
            return;
        }

        // Check if already friends
        List<User> friends = sessionFacade.getFriends(currentUser.getId());
        boolean isFriend = friends.stream().anyMatch(f -> f.getId() == viewedUser.getId());

        if (isFriend) {
            alreadyFriendsButton.setVisible(true);
            alreadyFriendsButton.setManaged(true);
            return;
        }

        // Check if there's a pending request
        List<FriendRequest> sentRequests = sessionFacade.getSentRequests(currentUser.getId());
        boolean hasPendingRequest = sentRequests.stream()
                .anyMatch(r -> r.getReceiverId() == viewedUser.getId() &&
                        r.getStatus() == FriendRequestStatus.PENDING);

        if (hasPendingRequest) {
            pendingRequestButton.setVisible(true);
            pendingRequestButton.setManaged(true);
            return;
        }

        // Show send friend request button
        friendRequestButton.setVisible(true);
        friendRequestButton.setManaged(true);
    }

    @FXML
    protected void onSendFriendRequestClick() {
        if (sessionFacade.sendFriendRequest(currentUser.getId(), viewedUser.getId())) {
            showSuccess("Demande d'ami envoyée à " + viewedUser.getFullname());
            updateFriendshipStatus();
        } else {
            showError("Impossible d'envoyer la demande");
        }
    }

    private void onViewResourceClick(Resource resource) {
        resourceFacade.viewResource(resource.getId());

        MainAppController mainController = MainAppController.getInstance();
        if (mainController != null) {
            ResourceDetailController.setResourceToLoad(resource);
            mainController.loadContent("/dev/studylink/studylink/resource-detail-view.fxml");
        }
    }

    @FXML
    protected void onBackClick() {
        MainAppController.loadContentStatic("/dev/studylink/studylink/friends-content.fxml");
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
