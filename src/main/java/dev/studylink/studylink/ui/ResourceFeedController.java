package dev.studylink.studylink.ui;

import java.io.IOException;
import java.util.List;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.business.ResourceFacade;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResourceFeedController {
    @FXML
    private ListView<Resource> resourcesListView;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<Category> categoryFilter;

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

        setupResourcesListView();
        loadCategories();
        
        // Charger les ressources de manière asynchrone pour éviter le blocage
        javafx.application.Platform.runLater(this::loadResources);
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
                vbox.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8;");

                // Title
                Label titleLabel = new Label(resource.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A1640;");

                // Owner name - Cache pour éviter les requêtes répétées
                Label ownerLabel = new Label();
                try {
                    User owner = sessionFacade.getUserById(resource.getOwnerId());
                    ownerLabel.setText("By: " + owner.getFullname());
                } catch (Exception e) {
                    ownerLabel.setText("By: Unknown");
                }
                ownerLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");

                // Categories
                HBox categoriesBox = new HBox(5);
                for (Category cat : resource.getCategories()) {
                    Label catLabel = new Label(cat.getTitle());
                    catLabel.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    categoriesBox.getChildren().add(catLabel);
                }

                // Stats
                HBox statsBox = new HBox(20);
                Label viewsLabel = new Label("👁 " + resource.getViewCount() + " views");
                Label savesLabel = new Label("⭐ " + resource.getSaveCount() + " saves");

                if (resource.getPrice() > 0) {
                    Label priceLabel = new Label("💰 $" + String.format("%.2f", resource.getPrice()));
                    priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    statsBox.getChildren().addAll(viewsLabel, savesLabel, priceLabel);
                } else {
                    Label freeLabel = new Label("🆓 FREE");
                    freeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    statsBox.getChildren().addAll(viewsLabel, savesLabel, freeLabel);
                }

                // Action buttons
                HBox buttonsBox = new HBox(10);
                Button viewButton = new Button("View Details");
                viewButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; -fx-font-weight: bold;");
                viewButton.setOnAction(e -> onViewResourceClick(resource));

                Button saveButton = new Button(resourceFacade.isResourceSaved(resource.getId()) ? "Saved ✓" : "Save");
                saveButton.setStyle("-fx-background-color: " +
                        (resourceFacade.isResourceSaved(resource.getId()) ? "#4CAF50" : "#E0E0E0") +
                        "; -fx-text-fill: " +
                        (resourceFacade.isResourceSaved(resource.getId()) ? "white" : "#333333") +
                        "; -fx-font-weight: bold;");
                saveButton.setOnAction(e -> onSaveResourceClick(resource));

                buttonsBox.getChildren().addAll(viewButton, saveButton);

                vbox.getChildren().addAll(titleLabel, ownerLabel, categoriesBox, statsBox, buttonsBox);
                setGraphic(vbox);
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = sessionFacade.getAllCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
        categoryFilter.setItems(categoryList);
    }

    private void loadResources() {
        List<Resource> resources = resourceFacade.getResourceFeed();
        ObservableList<Resource> resourceList = FXCollections.observableArrayList(resources);
        resourcesListView.setItems(resourceList);
    }

    @FXML
    protected void onCreateResourceClick() {
        // Charger dans la zone de contenu au lieu de remplacer toute la scène
        MainAppController.loadContentStatic("/dev/studylink/studylink/create-resource-view.fxml");
    }

    @FXML
    protected void onSearchClick() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            loadResources();
            return;
        }

        List<Resource> results = resourceFacade.searchResources(query);
        ObservableList<Resource> resourceList = FXCollections.observableArrayList(results);
        resourcesListView.setItems(resourceList);

        if (results.isEmpty()) {
            showError("No resources found for '" + query + "'");
        } else {
            showSuccess("Found " + results.size() + " resource(s)");
        }
    }

    @FXML
    protected void onFilterByCategoryClick() {
        Category selectedCategory = categoryFilter.getValue();

        if (selectedCategory == null) {
            loadResources();
            return;
        }

        List<Resource> results = resourceFacade.getResourcesByCategory(selectedCategory.getId());
        ObservableList<Resource> resourceList = FXCollections.observableArrayList(results);
        resourcesListView.setItems(resourceList);

        if (results.isEmpty()) {
            showError("No resources in this category");
        } else {
            showSuccess("Found " + results.size() + " resource(s) in " + selectedCategory.getTitle());
        }
    }

    @FXML
    protected void onRefreshClick() {
        searchField.clear();
        categoryFilter.setValue(null);
        loadResources();
        showSuccess("Resource feed refreshed");
    }


    private void onViewResourceClick(Resource resource) {
        resourceFacade.viewResource(resource.getId());

        MainAppController mainController = MainAppController.getInstance();
        if (mainController != null) {
            ResourceDetailController.setResourceToLoad(resource);
            mainController.loadContent("/dev/studylink/studylink/resource-detail-view.fxml");
        }
    }

    private void onSaveResourceClick(Resource resource) {
        try {
            boolean isSaved = resourceFacade.isResourceSaved(resource.getId());

            if (isSaved) {
                if (resourceFacade.unsaveResource(resource.getId())) {
                    showSuccess("Resource removed from saved");
                    loadResources();
                } else {
                    showError("Failed to unsave resource");
                }
            } else {
                if (resourceFacade.saveResource(resource.getId())) {
                    showSuccess("Resource saved!");
                    loadResources();
                } else {
                    showError("Resource is already saved");
                }
            }
        } catch (ResourceNotFoundException e) {
            showError("Resource not found");
        }
    }

    @FXML
    protected void onMyResourcesClick() {
        MainAppController.loadContentStatic("/dev/studylink/studylink/my-resources-view.fxml");
    }

    @FXML
    protected void onBackClick() {
        MainAppController.loadContentStatic("/dev/studylink/studylink/profile-content.fxml");
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