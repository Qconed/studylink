package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.FriendRequest;
import dev.studylink.studylink.business.FriendRequestStatus;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class FriendRequestController {
    @FXML
    private TextField searchField;

    @FXML
    private ListView<User> searchResultsListView;

    @FXML
    private ListView<FriendRequest> friendRequestsListView;

    @FXML
    private ListView<User> friendsListView;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        setupSearchResultsListView();
        setupFriendRequestsListView();
        setupFriendsListView();

        loadFriendRequests();
        loadFriendsList();
    }

    private void setupSearchResultsListView() {
        searchResultsListView.setCellFactory(param -> new ListCell<User>() {
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

                // Bouton pour voir le profil
                Button viewProfileButton = new Button("Voir profil");
                viewProfileButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white;");
                viewProfileButton.setOnAction(e -> onViewUserProfileClick(user));

                // Bouton pour envoyer une demande
                Button sendRequestButton = new Button("Envoyer une demande");
                sendRequestButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                sendRequestButton.setOnAction(e -> onSendFriendRequestClick(user));

                hbox.getChildren().addAll(nameLabel, emailLabel, viewProfileButton, sendRequestButton);
                setGraphic(hbox);
            }
        });
    }


    private void setupFriendRequestsListView() {
        friendRequestsListView.setCellFactory(param -> new ListCell<FriendRequest>() {
            @Override
            protected void updateItem(FriendRequest request, boolean empty) {
                super.updateItem(request, empty);

                if (empty || request == null) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(5));

                Label senderLabel = new Label(request.getSenderName());
                senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #D3D2D7;");

                Button acceptButton = new Button("Accepter");
                acceptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                acceptButton.setOnAction(e -> onAcceptRequestClick(request));

                Button rejectButton = new Button("Refuser");
                rejectButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                rejectButton.setOnAction(e -> onRejectRequestClick(request));

                hbox.getChildren().addAll(senderLabel, acceptButton, rejectButton);
                setGraphic(hbox);
            }
        });
    }

    private void setupFriendsListView() {
        friendsListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User friend, boolean empty) {
                super.updateItem(friend, empty);

                if (empty || friend == null) {
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(5));

                Label nameLabel = new Label(friend.getFullname());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #D3D2D7; -fx-min-width: 150;");

                Label emailLabel = new Label(friend.getEmail());
                emailLabel.setStyle("-fx-text-fill: #888888; -fx-min-width: 200;");

                // Bouton pour voir le profil
                Button viewProfileButton = new Button("Voir profil");
                viewProfileButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white;");
                viewProfileButton.setOnAction(e -> onViewUserProfileClick(friend));

                // Bouton pour retirer
                Button removeButton = new Button("Retirer");
                removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                removeButton.setOnAction(e -> onRemoveFriendClick(friend));

                hbox.getChildren().addAll(nameLabel, emailLabel, viewProfileButton, removeButton);
                setGraphic(hbox);
            }
        });
    }

    @FXML
    protected void onSearchUserClick() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            showError("Veuillez entrer un nom ou email");
            return;
        }

        List<User> results = sessionFacade.searchUsers(query);

        // Filter out current user and existing friends
        results.removeIf(user ->
                user.getId() == currentUser.getId() ||
                        sessionFacade.getFriends(currentUser.getId()).contains(user)
        );

        ObservableList<User> userList = FXCollections.observableArrayList(results);
        searchResultsListView.setItems(userList);

        if (results.isEmpty()) {
            showError("Aucun utilisateur trouvé");
        }
    }

    private void onSendFriendRequestClick(User targetUser) {
        if (sessionFacade.sendFriendRequest(currentUser.getId(), targetUser.getId())) {
            showSuccess("Demande envoyée à " + targetUser.getFullname());
            searchResultsListView.getItems().remove(targetUser);
        } else {
            showError("Impossible d'envoyer la demande");
        }
    }

    private void onAcceptRequestClick(FriendRequest request) {
        if (sessionFacade.acceptFriendRequest(request.getId())) {
            showSuccess("Demande acceptée!");
            loadFriendRequests();
            loadFriendsList();
        } else {
            showError("Erreur lors de l'acceptation");
        }
    }

    private void onRejectRequestClick(FriendRequest request) {
        if (sessionFacade.rejectFriendRequest(request.getId())) {
            showSuccess("Demande refusée");
            loadFriendRequests();
        } else {
            showError("Erreur lors du refus");
        }
    }

    private void onRemoveFriendClick(User friend) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer");
        confirmAlert.setHeaderText("Retirer " + friend.getFullname() + " de vos amis?");
        confirmAlert.setContentText("Cette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (sessionFacade.removeFriend(currentUser.getId(), friend.getId())) {
                    showSuccess("Ami retiré");
                    loadFriendsList();
                } else {
                    showError("Erreur lors de la suppression");
                }
            }
        });
    }

    private void loadFriendRequests() {
        List<FriendRequest> requests = sessionFacade.getFriendRequests(currentUser.getId());
        ObservableList<FriendRequest> requestList = FXCollections.observableArrayList(requests);
        friendRequestsListView.setItems(requestList);
    }

    private void loadFriendsList() {
        List<User> friends = sessionFacade.getFriends(currentUser.getId());
        ObservableList<User> friendList = FXCollections.observableArrayList(friends);
        friendsListView.setItems(friendList);
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/dev/studylink/studylink/main-app-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'accueil");
        }
    }

    private void onViewUserProfileClick(User user) {
        MainAppController mainController = MainAppController.getInstance();
        if (mainController != null) {
            UserProfileController.setUserToLoad(user);
            mainController.loadContent("/dev/studylink/studylink/user-profile-view.fxml");
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
