package dev.studylink.studylink.ui;

import java.io.IOException;
import java.util.List;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.business.ResourceFacade;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import dev.studylink.studylink.exception.UnauthorizedResourceAccessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MyResourcesController {
    @FXML
    private ListView<Resource> myResourcesListView;

    @FXML
    private ListView<Resource> savedResourcesListView;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        setupMyResourcesListView();
        setupSavedResourcesListView();

        loadMyResources();
        loadSavedResources();
    }

    private void setupMyResourcesListView() {
        myResourcesListView.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);

                if (empty || resource == null) {
                    setGraphic(null);
                    return;
                }

                VBox vbox = new VBox(10);
                vbox.getStyleClass().add("resource-cell");
                vbox.setPadding(new Insets(10));

                // Title
                Label titleLabel = new Label(resource.getTitle());
                titleLabel.getStyleClass().add("resource-cell-title");
                titleLabel.setWrapText(true);

                // Categories
                HBox categoriesBox = new HBox(5);
                for (Category cat : resource.getCategories()) {
                    Label catLabel = new Label(cat.getTitle());
                    catLabel.getStyleClass().add("category-badge");
                    categoriesBox.getChildren().add(catLabel);
                }

                // Stats
                HBox statsBox = new HBox(20);
                Label viewsLabel = new Label("👁 " + resource.getViewCount());
                Label savesLabel = new Label("⭐ " + resource.getSaveCount());

                Label priceLabel = new Label();
                priceLabel.getStyleClass().add("price-label");
                if (resource.getPrice() > 0) {
                    priceLabel.setText("💰 $" + String.format("%.2f", resource.getPrice()));
                } else {
                    priceLabel.setText("🆓 FREE");
                }

                statsBox.getChildren().addAll(viewsLabel, savesLabel, priceLabel);

                // Action buttons
                HBox buttonsBox = new HBox(10);

                Button viewButton = new Button("View");
                viewButton.getStyleClass().add("resource-cell-btn-view");
                viewButton.setOnAction(e -> onViewResourceClick(resource));

                Button editButton = new Button("Edit");
                editButton.getStyleClass().add("resource-cell-btn-edit");
                editButton.setOnAction(e -> onEditResourceClick(resource));

                Button deleteButton = new Button("Delete");
                deleteButton.getStyleClass().add("resource-cell-btn-delete");
                deleteButton.setOnAction(e -> onDeleteResourceClick(resource));

                buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);

                vbox.getChildren().addAll(titleLabel, categoriesBox, statsBox, buttonsBox);
                setGraphic(vbox);
            }
        });
    }

    private void setupSavedResourcesListView() {
        savedResourcesListView.setCellFactory(param -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);

                if (empty || resource == null) {
                    setGraphic(null);
                    return;
                }

                VBox vbox = new VBox(10);
                vbox.getStyleClass().add("resource-cell");
                vbox.setPadding(new Insets(10));

                // Title
                Label titleLabel = new Label(resource.getTitle());
                titleLabel.getStyleClass().add("resource-cell-title");
                titleLabel.setWrapText(true);

                // Owner
                Label ownerLabel = new Label();
                ownerLabel.getStyleClass().add("resource-owner");
                try {
                    User owner = sessionFacade.getUserById(resource.getOwnerId());
                    ownerLabel.setText("By: " + owner.getFullname());
                } catch (Exception e) {
                    ownerLabel.setText("By: Unknown");
                }

                // Categories
                HBox categoriesBox = new HBox(5);
                for (Category cat : resource.getCategories()) {
                    Label catLabel = new Label(cat.getTitle());
                    catLabel.getStyleClass().add("category-badge");
                    categoriesBox.getChildren().add(catLabel);
                }

                // Action button
                Button viewButton = new Button("View Details");
                viewButton.getStyleClass().add("resource-cell-btn-view");
                viewButton.setOnAction(e -> onViewSavedResourceClick(resource));

                vbox.getChildren().addAll(titleLabel, ownerLabel, categoriesBox, viewButton);
                setGraphic(vbox);
            }
        });
    }

    private void loadMyResources() {
        List<Resource> resources = resourceFacade.getMyResources();
        ObservableList<Resource> resourceList = FXCollections.observableArrayList(resources);
        myResourcesListView.setItems(resourceList);
    }

    private void loadSavedResources() {
        List<Resource> resources = resourceFacade.getMySavedResources();
        ObservableList<Resource> resourceList = FXCollections.observableArrayList(resources);
        savedResourcesListView.setItems(resourceList);
    }

    protected void onEditResourceClick(Resource resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/edit-resource-view.fxml"));
            Scene scene = new Scene(loader.load());
            
            EditResourceController editController = loader.getController();
            editController.setResource(resource);
            
            Stage stage = (Stage) myResourcesListView.getScene().getWindow();
            stage.setTitle("Edit Resource - " + resource.getTitle());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading edit resource view: " + e.getMessage());
        }
    }

    protected void onDeleteResourceClick(Resource resource) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Resource");
        confirmAlert.setHeaderText("Delete '" + resource.getTitle() + "'?");
        confirmAlert.setContentText("This action cannot be undone. All comments will be deleted.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (resourceFacade.deleteResourcePost(resource.getId())) {
                        showSuccess("Resource deleted successfully");
                        loadMyResources();
                    } else {
                        showError("Failed to delete resource");
                    }
                } catch (ResourceNotFoundException e) {
                    showError("Resource not found");
                } catch (UnauthorizedResourceAccessException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    protected void onViewResourceClick(Resource resource) {
        navigateToResourceDetail(resource);
    }

    protected void onViewSavedResourceClick(Resource resource) {
        navigateToResourceDetail(resource);
    }

    private void navigateToResourceDetail(Resource resource) {
        resourceFacade.viewResource(resource.getId());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/resource-detail-view.fxml"));
            Scene scene = new Scene(loader.load());

            ResourceDetailController controller = loader.getController();
            controller.setResource(resource);

            Stage stage = (Stage) myResourcesListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading resource details");
        }
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/main-app-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) myResourcesListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading main page");
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
