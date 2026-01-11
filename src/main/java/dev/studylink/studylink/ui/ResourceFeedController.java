package dev.studylink.studylink.ui;

import java.util.List;
import dev.studylink.studylink.business.*;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResourceFeedController {
    @FXML private ListView<Resource> resourcesListView;
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();
        setupResourcesListView();
        loadCategoriesAsync();
        loadResourcesAsync();
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

                Label titleLabel = new Label(resource.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A1640;");

                // ✅ PAS DE REQUÊTE SQL! Le nom est déjà chargé!
                String ownerName = resource.getOwnerName() != null ? resource.getOwnerName() : "Unknown";
                Label ownerLabel = new Label("By: " + ownerName);
                ownerLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");

                HBox categoriesBox = new HBox(5);
                for (Category cat : resource.getCategories()) {
                    Label catLabel = new Label(cat.getTitle());
                    catLabel.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    categoriesBox.getChildren().add(catLabel);
                }

                HBox statsBox = new HBox(20);
                statsBox.getChildren().addAll(
                        new Label("👁 " + resource.getViewCount() + " views"),
                        new Label("⭐ " + resource.getSaveCount() + " saves"),
                        createPriceLabel(resource)
                );

                HBox buttonsBox = new HBox(10);
                Button viewButton = new Button("View Details");
                viewButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; -fx-font-weight: bold;");
                viewButton.setOnAction(e -> onViewResourceClick(resource));

                Button saveButton = createSaveButton(resource);
                buttonsBox.getChildren().addAll(viewButton, saveButton);

                vbox.getChildren().addAll(titleLabel, ownerLabel, categoriesBox, statsBox, buttonsBox);
                setGraphic(vbox);
            }
        });
    }

    private Label createPriceLabel(Resource resource) {
        Label priceLabel = new Label();
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        if (resource.getPrice() > 0) {
            priceLabel.setText("💰 $" + String.format("%.2f", resource.getPrice()));
        } else {
            priceLabel.setText("🆓 FREE");
        }
        return priceLabel;
    }

    private Button createSaveButton(Resource resource) {
        boolean isSaved = resourceFacade.isResourceSaved(resource.getId());
        Button saveButton = new Button(isSaved ? "Saved ✓" : "Save");
        saveButton.setStyle("-fx-background-color: " + (isSaved ? "#4CAF50" : "#E0E0E0") +
                "; -fx-text-fill: " + (isSaved ? "white" : "#333333") +
                "; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> onSaveResourceClick(resource));
        return saveButton;
    }

    private void loadCategoriesAsync() {
        new Thread(() -> {
            List<Category> categories = sessionFacade.getAllCategories();
            Platform.runLater(() -> {
                categoryFilter.setItems(FXCollections.observableArrayList(categories));
            });
        }).start();
    }

    private void loadResourcesAsync() {
        resourcesListView.setPlaceholder(new Label("Loading resources..."));

        new Thread(() -> {
            List<Resource> resources = resourceFacade.getResourceFeed();
            Platform.runLater(() -> {
                resourcesListView.setItems(FXCollections.observableArrayList(resources));
                if (resources.isEmpty()) {
                    resourcesListView.setPlaceholder(new Label("No resources available"));
                }
            });
        }).start();
    }

    @FXML
    protected void onCreateResourceClick() {
        MainAppController.loadContentStatic("/dev/studylink/studylink/create-resource-view.fxml");
    }

    @FXML
    protected void onSearchClick() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadResourcesAsync();
            return;
        }

        new Thread(() -> {
            List<Resource> results = resourceFacade.searchResources(query);
            Platform.runLater(() -> {
                resourcesListView.setItems(FXCollections.observableArrayList(results));
                if (results.isEmpty()) {
                    showError("No resources found for '" + query + "'");
                } else {
                    showSuccess("Found " + results.size() + " resource(s)");
                }
            });
        }).start();
    }

    @FXML
    protected void onFilterByCategoryClick() {
        Category selectedCategory = categoryFilter.getValue();
        if (selectedCategory == null) {
            loadResourcesAsync();
            return;
        }

        new Thread(() -> {
            List<Resource> results = resourceFacade.getResourcesByCategory(selectedCategory.getId());
            Platform.runLater(() -> {
                resourcesListView.setItems(FXCollections.observableArrayList(results));
                if (results.isEmpty()) {
                    showError("No resources in this category");
                } else {
                    showSuccess("Found " + results.size() + " resource(s) in " + selectedCategory.getTitle());
                }
            });
        }).start();
    }

    @FXML
    protected void onRefreshClick() {
        searchField.clear();
        categoryFilter.setValue(null);
        loadResourcesAsync();
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
        new Thread(() -> {
            try {
                boolean isSaved = resourceFacade.isResourceSaved(resource.getId());
                if (isSaved) {
                    resourceFacade.unsaveResource(resource.getId());
                    Platform.runLater(() -> {
                        showSuccess("Resource removed from saved");
                        loadResourcesAsync();
                    });
                } else {
                    resourceFacade.saveResource(resource.getId());
                    Platform.runLater(() -> {
                        showSuccess("Resource saved!");
                        loadResourcesAsync();
                    });
                }
            } catch (ResourceNotFoundException e) {
                Platform.runLater(() -> showError("Resource not found"));
            }
        }).start();
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
