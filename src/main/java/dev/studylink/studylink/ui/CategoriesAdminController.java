package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.UnauthorizedException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class CategoriesAdminController {
    @FXML
    private ListView<Category> categoriesListView;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<CategoryType> typeComboBox;

    @FXML
    private Spinner<Integer> levelSpinner;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private User adminUser;
    private Category selectedCategory;

    @FXML
    public void initialize() {
        adminUser = sessionFacade.getCurrentUser();

        // Check if user is admin
        if (adminUser == null || adminUser.getRole() != Role.ADMIN) {
            showError("Accès non autorisé. Vous devez être administrateur.");
            return;
        }

        // Setup type combo box
        typeComboBox.setItems(FXCollections.observableArrayList(CategoryType.values()));

        // Setup level spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        levelSpinner.setValueFactory(valueFactory);

        // Setup categories list view
        setupCategoriesListView();

        loadAllCategories();
    }

    private void setupCategoriesListView() {
        categoriesListView.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);

                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setPadding(new Insets(10));

                    Label titleLabel = new Label(category.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label typeLabel = new Label("Type: " + category.getType());
                    typeLabel.setStyle("-fx-text-fill: #666666;");

                    Label levelLabel = new Label("Niveau: " + category.getLevel());
                    levelLabel.setStyle("-fx-text-fill: #666666;");

                    vbox.getChildren().addAll(titleLabel, typeLabel, levelLabel);

                    HBox hbox = new HBox(15);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.getChildren().add(vbox);

                    setGraphic(hbox);
                }
            }
        });

        categoriesListView.setOnMouseClicked(event -> {
            Category category = categoriesListView.getSelectionModel().getSelectedItem();
            if (category != null) {
                onSelectCategory(category);
            }
        });
    }

    private void onSelectCategory(Category category) {
        selectedCategory = category;
        titleField.setText(category.getTitle());
        descriptionArea.setText(category.getDescription());
        typeComboBox.setValue(category.getType());
        levelSpinner.getValueFactory().setValue(category.getLevel());
        showSuccess("Catégorie sélectionnée: " + category.getTitle());
    }

    @FXML
    protected void onCreateCategoryClick() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        CategoryType type = typeComboBox.getValue();
        Integer level = levelSpinner.getValue();

        if (title.isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }

        if (type == null) {
            showError("Le type est obligatoire");
            return;
        }

        try {
            boolean success = sessionFacade.createCategory(adminUser.getId(), title, description, type, level);
            if (success) {
                showSuccess("Catégorie créée avec succès!");
                clearForm();
                loadAllCategories();
            } else {
                showError("Erreur lors de la création de la catégorie");
            }
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onUpdateCategoryClick() {
        if (selectedCategory == null) {
            showError("Veuillez sélectionner une catégorie à modifier");
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        CategoryType type = typeComboBox.getValue();
        Integer level = levelSpinner.getValue();

        if (title.isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }

        if (type == null) {
            showError("Le type est obligatoire");
            return;
        }

        // Create a new Category object with updated values
        Category updatedCategory = new Category(selectedCategory.getId(), title, description, type, level);

        try {
            boolean success = sessionFacade.updateCategory(adminUser.getId(), updatedCategory);
            if (success) {
                showSuccess("Catégorie modifiée avec succès!");
                clearForm();
                loadAllCategories();
            } else {
                showError("Erreur lors de la modification de la catégorie");
            }
        } catch (UnauthorizedException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onDeleteCategoryClick() {
        if (selectedCategory == null) {
            showError("Veuillez sélectionner une catégorie à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer " + selectedCategory.getTitle() + "?");
        confirmAlert.setContentText("Cette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = sessionFacade.deleteCategory(adminUser.getId(), selectedCategory.getId());
                    if (success) {
                        showSuccess("Catégorie supprimée avec succès!");
                        clearForm();
                        loadAllCategories();
                    } else {
                        showError("Erreur lors de la suppression de la catégorie");
                    }
                } catch (UnauthorizedException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void onClearFormClick() {
        clearForm();
    }

    private void clearForm() {
        selectedCategory = null;
        titleField.clear();
        descriptionArea.clear();
        typeComboBox.setValue(null);
        levelSpinner.getValueFactory().setValue(1);
        categoriesListView.getSelectionModel().clearSelection();
    }

    private void loadAllCategories() {
        List<Category> categories = sessionFacade.getAllCategories();
        categoriesListView.setItems(FXCollections.observableArrayList(categories));
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
