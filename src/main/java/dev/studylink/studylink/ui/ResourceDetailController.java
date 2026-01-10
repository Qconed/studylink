package dev.studylink.studylink.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Comment;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.business.ResourceFacade;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.exception.InvalidResourceDataException;
import dev.studylink.studylink.exception.ResourceNotFoundException;
import dev.studylink.studylink.exception.UnauthorizedResourceAccessException;
import dev.studylink.studylink.exception.UserDoesNotExist;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ResourceDetailController {
    @FXML
    private Label titleLabel;

    @FXML
    private Label ownerLabel;

    @FXML
    private TextArea contentArea;

    @FXML
    private FlowPane categoriesFlowPane;

    @FXML
    private Button attachmentButton;

    @FXML
    private Label priceLabel;

    @FXML
    private Label viewCountLabel;

    @FXML
    private Label saveCountLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private ListView<Comment> commentsListView;

    @FXML
    private TextField commentField;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final ResourceFacade resourceFacade = ResourceFacade.getInstance();
    private final SessionFacade sessionFacade = SessionFacade.getInstance();
    private Resource currentResource;
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = sessionFacade.getCurrentUser();

        contentArea.setEditable(false);
        contentArea.setWrapText(true);

        setupCommentsListView();
    }

    public void setResource(Resource resource) {
        this.currentResource = resource;
        
        // Track view only if different user
        resourceFacade.viewResource(resource.getId());
        
        displayResourceDetails();
        loadComments();
        updateSaveButton();
    }

    private void displayResourceDetails() {
        if (currentResource == null) return;

        titleLabel.setText(currentResource.getTitle());
        contentArea.setText(currentResource.getContent());

        // Owner
        try {
            User owner = sessionFacade.getUserById(currentResource.getOwnerId());
            ownerLabel.setText("By: " + owner.getFullname());
        } catch (UserDoesNotExist e) {
            ownerLabel.setText("By: Unknown");
        }

        // Categories
        categoriesFlowPane.getChildren().clear();
        for (Category cat : currentResource.getCategories()) {
            Label catLabel = new Label(cat.getTitle());
            catLabel.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; " +
                    "-fx-padding: 5 10; -fx-background-radius: 15;");
            categoriesFlowPane.getChildren().add(catLabel);
        }

        // Price
        if (currentResource.isFree()) {
            priceLabel.setText("FREE");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 18px;");
        } else {
            priceLabel.setText("$" + String.format("%.2f", currentResource.getPrice()));
            priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 18px;");
        }

        // Stats
        viewCountLabel.setText(currentResource.getViewCount() + " views");
        saveCountLabel.setText(currentResource.getSaveCount() + " saves");

        // Created at
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        createdAtLabel.setText("Created: " + currentResource.getCreatedAt().format(formatter));

        // Attachment button
        if (currentResource.getAttachmentPath() != null && !currentResource.getAttachmentPath().isEmpty()) {
            attachmentButton.setVisible(true);
            attachmentButton.setText("📎 Download Attachment");
        } else {
            attachmentButton.setVisible(false);
        }
    }

    private void setupCommentsListView() {
        commentsListView.setCellFactory(param -> new ListCell<Comment>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);

                if (empty || comment == null) {
                    setGraphic(null);
                    return;
                }

                VBox vbox = new VBox(5);
                vbox.setPadding(new Insets(8));
                vbox.setStyle("-fx-background-color: #F5F5F5; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox headerBox = new HBox(10);
                headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label authorLabel = new Label(comment.getAuthorName() != null ? comment.getAuthorName() : "Unknown");
                authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4618F4;");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
                Label timestampLabel = new Label(comment.getTimestamp().format(formatter));
                timestampLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                headerBox.getChildren().addAll(authorLabel, timestampLabel);

                // Delete button if current user is the author
                if (currentUser != null && comment.getAuthorId() == currentUser.getId()) {
                    Button deleteButton = new Button("Delete");
                    deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 10px;");
                    deleteButton.setOnAction(e -> onDeleteCommentClick(comment));
                    headerBox.getChildren().add(deleteButton);
                }

                Label contentLabel = new Label(comment.getContent());
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-text-fill: #333333;");

                vbox.getChildren().addAll(headerBox, contentLabel);
                setGraphic(vbox);
            }
        });
    }

    private void loadComments() {
        try {
            List<Comment> comments = resourceFacade.getResourceComments(currentResource.getId());
            ObservableList<Comment> commentList = FXCollections.observableArrayList(comments);
            commentsListView.setItems(commentList);
        } catch (ResourceNotFoundException e) {
            showError("Resource not found");
        }
    }

    @FXML
    protected void onDownloadAttachmentClick() {
        if (currentResource.getAttachmentPath() == null || currentResource.getAttachmentPath().isEmpty()) {
            showError("No attachment available");
            return;
        }

        try {
            // Prepare file chooser to save file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Attachment");
            
            // Set initial file name from attachment path
            File attachmentFile = new File(currentResource.getAttachmentPath());
            fileChooser.setInitialFileName(attachmentFile.getName());
            
            Stage stage = (Stage) attachmentButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            if (selectedFile != null) {
                // Copy file from attachment path to user-selected location
                Path source = Paths.get(currentResource.getAttachmentPath());
                Path destination = Paths.get(selectedFile.getAbsolutePath());
                
                Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                showSuccess("File downloaded successfully to: " + selectedFile.getName());
            }
        } catch (IOException e) {
            showError("Error downloading file: " + e.getMessage());
        }
    }

    @FXML
    protected void onSaveResourceClick() {
        try {
            boolean isSaved = resourceFacade.isResourceSaved(currentResource.getId());

            if (isSaved) {
                // Unsave
                if (resourceFacade.unsaveResource(currentResource.getId())) {
                    showSuccess("Resource removed from saved");
                    updateSaveButton();

                    // Refresh resource to update save count
                    currentResource = resourceFacade.getResourceById(currentResource.getId());
                    displayResourceDetails();
                }
            } else {
                // Save
                if (resourceFacade.saveResource(currentResource.getId())) {
                    showSuccess("Resource saved!");
                    updateSaveButton();

                    // Refresh resource to update save count
                    currentResource = resourceFacade.getResourceById(currentResource.getId());
                    displayResourceDetails();
                }
            }
        } catch (ResourceNotFoundException e) {
            showError("Resource not found");
        }
    }

    private void updateSaveButton() {
        boolean isSaved = resourceFacade.isResourceSaved(currentResource.getId());

        if (isSaved) {
            saveButton.setText("Saved ✓");
            saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            saveButton.setText("Save Resource");
            saveButton.setStyle("-fx-background-color: #4618F4; -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    @FXML
    protected void onAddCommentClick() {
        String content = commentField.getText().trim();

        if (content.isEmpty()) {
            showError("Comment cannot be empty");
            return;
        }

        try {
            Comment comment = resourceFacade.addCommentToResource(currentResource.getId(), content);

            if (comment != null) {
                showSuccess("Comment added!");
                commentField.clear();
                loadComments();
            } else {
                showError("Failed to add comment");
            }
        } catch (ResourceNotFoundException e) {
            showError("Resource not found");
        } catch (InvalidResourceDataException e) {
            showError(e.getMessage());
        }
    }

    protected void onDeleteCommentClick(Comment comment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Comment");
        confirmAlert.setHeaderText("Delete this comment?");
        confirmAlert.setContentText("This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (resourceFacade.deleteComment(comment.getId())) {
                        showSuccess("Comment deleted");
                        loadComments();
                    } else {
                        showError("Failed to delete comment");
                    }
                } catch (UnauthorizedResourceAccessException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/resource-feed-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading resource feed");
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
