package dev.studylink.studylink.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

public class StudySessionController {

    @FXML
    private ListView<String> createdSessionsList;

    @FXML
    private ListView<String> registeredSessionsList;

    @FXML
    private ListView<String> recommendedSessionsList;

    @FXML
    private ComboBox<String> categorySelector;

    @FXML
    public void initialize() {
        // Initialize lists and category selector
        loadCreatedSessions();
        loadRegisteredSessions();
        loadRecommendedSessions();
    }

    private void loadCreatedSessions() {
        // TODO: Fetch and display created sessions
        createdSessionsList.getItems().add("Session 1 (Created)");
    }

    private void loadRegisteredSessions() {
        // TODO: Fetch and display registered sessions
        registeredSessionsList.getItems().add("Session 2 (Registered)");
    }

    private void loadRecommendedSessions() {
        // TODO: Fetch and display recommended sessions
        recommendedSessionsList.getItems().add("Session 3 (Recommended)");
    }

    @FXML
    private void onCategoryChange() {
        // TODO: Update recommended sessions based on selected category
    }
}
