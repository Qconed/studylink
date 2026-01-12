package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.business.ResourceFacade;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import dev.studylink.studylink.exception.UnauthorizedResourceAccessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

public class EditResourceController {
    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private FlowPane selectedCategoriesPane;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private Resource currentResource;
    private List<Category> categories = new ArrayList<>();

    // Variable statique pour passer la ressource
    private static Resource resourceToLoad;

    @FXML
    public void initialize() {
        loadCategories();

        // Charger la ressource si elle a été définie
        if (resourceToLoad != null) {
            setResource(resourceToLoad);
            resourceToLoad = null;
        }
    }

    public static void setResourceToLoad(Resource resource) {
        resourceToLoad = resource;
    }

    public void setResource(Resource resource) {
        this.currentResource = resource;
        displayResourceData();
    }

    private void displayResourceData() {
        if (currentResource == null) return;

        titleField.setText(currentResource.getTitle());
        contentArea.setText(currentResource.getContent());

        categories = new ArrayList<>(currentResource.getCategories());
        displaySelectedCategories();
    }

    private void loadCategories() {
        List<Category> allCategories = sessionFacade.getAllCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(allCategories);
        categoryComboBox.setItems(categoryList);
    }

    @FXML
    protected void onAddCategoryClick() {
        Category selectedCategory = categoryComboBox.getValue();

        if (selectedCategory == null) {
            showError("Veuillez sélectionner une catégorie");
            return;
        }

        if (categories.contains(selectedCategory)) {
            showError("Catégorie déjà ajoutée");
            return;
        }

        categories.add(selectedCategory);
        displaySelectedCategories();
        categoryComboBox.setValue(null);
    }

    protected void onRemoveCategoryClick(Category category) {
        categories.remove(category);
        displaySelectedCategories();
    }

    private void displaySelectedCategories() {
        selectedCategoriesPane.getChildren().clear();

        for (Category category : categories) {
            Button categoryButton = new Button(category.getTitle() + " ✕");
            categoryButton.getStyleClass().add("category-badge");
            categoryButton.setOnAction(e -> onRemoveCategoryClick(category));
            selectedCategoriesPane.getChildren().add(categoryButton);
        }
    }

    @FXML
    protected void onSaveChangesClick() {
        if (!validateInputs()) {
            return;
        }

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        try {
            if (resourceFacade.updateResourcePost(currentResource.getId(), title, content, categories)) {
                showSuccess("Ressource mise à jour avec succès!");

                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::onCancelClick);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError("Erreur lors de la mise à jour");
            }
        } catch (ResourceNotFoundException e) {
            showError("Ressource introuvable");
        } catch (UnauthorizedResourceAccessException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onCancelClick() {
        MainAppController.loadContentStatic("/dev/studylink/studylink/my-resources-view.fxml");
    }

    private boolean validateInputs() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty()) {
            showError("Le titre est requis");
            return false;
        }

        if (title.length() > 200) {
            showError("Le titre ne peut pas dépasser 200 caractères");
            return false;
        }

        if (content.isEmpty()) {
            showError("Le contenu est requis");
            return false;
        }

        return true;
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