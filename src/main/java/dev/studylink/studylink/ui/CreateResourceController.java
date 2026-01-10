package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.business.ResourceFacade;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.exception.InvalidResourceDataException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateResourceController {
    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private FlowPane selectedCategoriesPane;

    @FXML
    private Button attachmentButton;

    @FXML
    private Label attachmentLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private File selectedFile;
    private List<Category> categories = new ArrayList<>();

    @FXML
    public void initialize() {
        loadCategories();

        // Default price to 0 (free)
        priceField.setText("0.0");

        // Allow only numbers in price field
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
    }

    private void loadCategories() {
        List<Category> allCategories = sessionFacade.getAllCategories();
        ObservableList<Category> categoryList = FXCollections.observableArrayList(allCategories);
        categoryComboBox.setItems(categoryList);
    }

    @FXML
    protected void onSelectAttachmentClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resource Attachment");

        // Add file filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.doc", "*.docx", "*.txt")
        );

        Stage stage = (Stage) attachmentButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            attachmentLabel.setText("Selected: " + selectedFile.getName());
            attachmentLabel.setStyle("-fx-text-fill: #4CAF50;");
        }
    }

    @FXML
    protected void onAddCategoryClick() {
        Category selectedCategory = categoryComboBox.getValue();

        if (selectedCategory == null) {
            showError("Please select a category");
            return;
        }

        if (categories.contains(selectedCategory)) {
            showError("Category already added");
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
            categoryButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; " +
                    "-fx-padding: 5 10; -fx-background-radius: 15; -fx-cursor: hand;");
            categoryButton.setOnAction(e -> onRemoveCategoryClick(category));

            selectedCategoriesPane.getChildren().add(categoryButton);
        }
    }

    @FXML
    protected void onCreateResourceClick() {
        if (!validateInputs()) {
            return;
        }

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        double price = Double.parseDouble(priceField.getText().trim());

        try {
            Resource resource = resourceFacade.createResourcePost(title, content, categories, selectedFile, price);

            showSuccess("Resource created successfully!");

            // Wait
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::onCancelClick);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (InvalidResourceDataException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    protected void onCancelClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/resource-feed-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading resource feed");
        }
    }

    private boolean validateInputs() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String priceText = priceField.getText().trim();

        if (title.isEmpty()) {
            showError("Title is required");
            return false;
        }

        if (title.length() > 200) {
            showError("Title cannot exceed 200 characters");
            return false;
        }

        if (content.isEmpty()) {
            showError("Content is required");
            return false;
        }

        if (priceText.isEmpty()) {
            showError("Price is required (use 0 for free resources)");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showError("Price cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid price format");
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
