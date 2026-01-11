package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.exception.UnauthorizedException;
import dev.studylink.studylink.impl.db.mysql.MySQLCategoryDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class StudySessionController {

    @FXML
    private ListView<String> createdSessionsList;

    @FXML
    private ListView<String> registeredSessionsList;

    @FXML
    private ListView<String> recommendedSessionsList;

    @FXML
    private ComboBox<String> categorySelector;

    private StudySessionFacade sessionFacade = StudySessionFacade.getInstance();

    @FXML
    public void initialize() {
        // Initialize lists and category selector
        loadCreatedSessions();
        loadRegisteredSessions();
        loadRecommendedSessions();
        
        // Print all sessions in terminal
        printAllSessions();
    }

    private void loadCreatedSessions() {
        try {
            List<StudySession> sessions = sessionFacade.listMySessions();
            createdSessionsList.getItems().clear();
            for (StudySession session : sessions) {
                createdSessionsList.getItems().add(formatSession(session));
            }
        } catch (UnauthorizedException e) {
            System.err.println("Error loading created sessions: " + e.getMessage());
        }
    }

    private void loadRegisteredSessions() {
        // TODO: Fetch and display registered sessions
        registeredSessionsList.getItems().add("Session 2 (Registered)");
    }

    private void loadRecommendedSessions() {
        // TODO: Fetch and display recommended sessions
        recommendedSessionsList.getItems().add("Session 3 (Recommended)");
    }

    private String formatSession(StudySession session) {
        TimeSlot timeSlot = session.getTimeSlot();
        String time = timeSlot != null && timeSlot.getStartTime() != null 
            ? timeSlot.getStartTime().toString() 
            : "No date";
        return String.format("%s - %s (Participants: %d-%d)", 
            session.getTitle(), time, session.getMinParticipants(), session.getMaxParticipants());
    }

    @FXML
    private void onCategoryChange() {
        // TODO: Update recommended sessions based on selected category
    }

    @FXML
    private void handleCreateSession() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/dev/studylink/studylink/create-session-dialog.fxml")
            );
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Create New Study Session");
            dialog.setScene(new Scene(loader.load()));
            
            CreateSessionDialogController controller = loader.getController();
            controller.setDialogStage(dialog);
            controller.setParentController(this);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening create session dialog: " + e.getMessage());
        }
    }

    public void refreshSessions() {
        loadCreatedSessions();
        loadRegisteredSessions();
        loadRecommendedSessions();
        printAllSessions();
    }

    private void printAllSessions() {
        System.out.println("\n========== ALL STUDY SESSIONS IN DATABASE ==========");
        List<StudySession> allSessions = sessionFacade.listAllSessions();
        
        if (allSessions.isEmpty()) {
            System.out.println("No sessions found in database.");
        } else {
            System.out.println("Total sessions: " + allSessions.size());
            System.out.println("---------------------------------------------------");
            
            for (StudySession session : allSessions) {
                System.out.println("\nSession ID: " + session.getId());
                System.out.println("  Title: " + session.getTitle());
                System.out.println("  Description: " + session.getDescription());
                System.out.println("  Organizer ID: " + session.getOrganizerId());
                System.out.println("  Tutored: " + (session.isTutored() ? "Yes" : "No"));
                System.out.println("  Price: " + session.getPrice() + "€");
                System.out.println("  Participants: " + session.getMinParticipants() + " - " + session.getMaxParticipants());
                System.out.println("  Status: " + session.getStatus());
                
                if (session.getTimeSlot() != null) {
                    TimeSlot ts = session.getTimeSlot();
                    System.out.println("  Start Time: " + ts.getStartTime());
                    System.out.println("  End Time: " + ts.getEndTime());
                    if (ts.getLocation() != null && !ts.getLocation().isEmpty()) {
                        System.out.println("  Location: " + ts.getLocation());
                    }
                }
                
                List<Integer> categoryIds = session.getCategoryIds();
                if (!categoryIds.isEmpty()) {
                    System.out.println("  Categories: " + categoryIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
                }
                
                System.out.println("  Created At: " + session.getCreatedAt());
            }
        }
        System.out.println("\n====================================================\n");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
