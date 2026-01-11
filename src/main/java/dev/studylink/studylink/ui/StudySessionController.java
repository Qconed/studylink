package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.exception.UnauthorizedException;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudySessionController {

    @FXML
    private ListView<StudySession> createdSessionsList;

    @FXML
    private ListView<StudySession> registeredSessionsList;

    @FXML
    private ListView<StudySession> recommendedSessionsList;

    @FXML
    private ComboBox<String> categorySelector;

    private StudySessionFacade sessionFacade = StudySessionFacade.getInstance();
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        // Initialize UserDAO
        userDAO = MySQLUserFactory.getInstance().createUserDAO();
        
        // Setup custom cell factories
        setupListCellFactories();
        
        // Initialize lists and category selector
        loadCreatedSessions();
        loadRegisteredSessions();
        loadRecommendedSessions();
        
        // Print all sessions in terminal
        printAllSessions();
    }

    private void setupListCellFactories() {
        createdSessionsList.setCellFactory(lv -> new StudySessionCell(true));
        registeredSessionsList.setCellFactory(lv -> new StudySessionCell(false));
        recommendedSessionsList.setCellFactory(lv -> new StudySessionCell(false));
    }

    private void loadCreatedSessions() {
        try {
            List<StudySession> sessions = sessionFacade.listMySessions();
            createdSessionsList.getItems().clear();
            createdSessionsList.getItems().addAll(sessions);
        } catch (UnauthorizedException e) {
            System.err.println("Error loading created sessions: " + e.getMessage());
        }
    }

    private void loadRegisteredSessions() {
        // TODO: Fetch and display registered sessions where user is participant
        // For now, show all sessions except user's own
        try {
            List<StudySession> allSessions = sessionFacade.listAllSessions();
            int currentUserId = SessionFacade.getInstance().getCurrentUser().getId();
            List<StudySession> otherSessions = allSessions.stream()
                .filter(s -> s.getOrganizerId() != currentUserId)
                .collect(Collectors.toList());
            registeredSessionsList.getItems().clear();
            registeredSessionsList.getItems().addAll(otherSessions);
        } catch (Exception e) {
            System.err.println("Error loading registered sessions: " + e.getMessage());
            // If not logged in, clear the list
            registeredSessionsList.getItems().clear();
        }
    }

    private void loadRecommendedSessions() {
        // TODO: Fetch and display recommended sessions based on user categories
        List<StudySession> allSessions = sessionFacade.listAllSessions();
        recommendedSessionsList.getItems().clear();
        recommendedSessionsList.getItems().addAll(allSessions);
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
            dialog.sizeToScene(); // Adapte la fenêtre au contenu
            
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

    // Inner class for custom cell rendering
    private class StudySessionCell extends ListCell<StudySession> {
        private final boolean isOrganizerList;

        public StudySessionCell(boolean isOrganizerList) {
            this.isOrganizerList = isOrganizerList;
        }

        @Override
        protected void updateItem(StudySession session, boolean empty) {
            super.updateItem(session, empty);

            if (empty || session == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox content = new VBox(5);
                content.setPadding(new Insets(5));

                // Title
                Label titleLabel = new Label(session.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                // Organizer name
                String organizerName = getOrganizerName(session.getOrganizerId());
                Label organizerLabel = new Label("Organizer: " + organizerName);
                organizerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                // Participants info
                int currentParticipants = sessionFacade.getParticipantsCount(session.getId());
                Label participantsLabel = new Label(
                    String.format("Participants: %d / %d (min: %d)",
                        currentParticipants,
                        session.getMaxParticipants(),
                        session.getMinParticipants())
                );
                participantsLabel.setStyle("-fx-font-size: 11px;");

                // Date/Time info
                TimeSlot ts = session.getTimeSlot();
                String dateTimeStr = "No date set";
                if (ts != null && ts.getStartTime() != null) {
                    dateTimeStr = String.format("%s - %s",
                        ts.getStartTime().toLocalDate().toString(),
                        ts.getStartTime().toLocalTime().toString());
                }
                Label dateLabel = new Label(dateTimeStr);
                dateLabel.setStyle("-fx-font-size: 11px;");

                // Price info
                if (session.isTutored() && session.getPrice() > 0) {
                    Label priceLabel = new Label(String.format("Price: %.2f\u20ac", session.getPrice()));
                    priceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: green;");
                    content.getChildren().add(priceLabel);
                } else if (!session.isTutored()) {
                    Label freeLabel = new Label("Free session");
                    freeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: green;");
                    content.getChildren().add(freeLabel);
                }

                // Add all labels
                content.getChildren().addAll(titleLabel, organizerLabel, participantsLabel, dateLabel);

                // Add join/leave button if not organizer
                if (!isOrganizerList) {
                    int currentUserId = -1;
                    try {
                        currentUserId = SessionFacade.getInstance().getCurrentUser().getId();
                    } catch (Exception e) {
                        // User not logged in, don't show button
                    }

                    if (currentUserId != -1 && session.getOrganizerId() != currentUserId) {
                        HBox buttonBox = new HBox();
                        buttonBox.setAlignment(Pos.CENTER_RIGHT);
                        buttonBox.setPadding(new Insets(5, 0, 0, 0));

                        // Check if user is already registered
                        boolean isRegistered = sessionFacade.isUserRegistered(session.getId(), currentUserId);

                        Button actionButton = new Button(isRegistered ? "Leave" : "Join");
                        actionButton.setStyle(isRegistered ?
                            "-fx-background-color: #d9534f; -fx-text-fill: white;" :
                            "-fx-background-color: #5cb85c; -fx-text-fill: white;");

                        actionButton.setOnAction(e -> {
                            handleSessionAction(session, isRegistered);
                        });

                        buttonBox.getChildren().add(actionButton);
                        content.getChildren().add(buttonBox);
                    }
                }

                setGraphic(content);
            }
        }

        private String getOrganizerName(int organizerId) {
            try {
                Optional<User> user = userDAO.findById(organizerId);
                return user.map(User::getFullname).orElse("Unknown");
            } catch (Exception e) {
                return "Unknown";
            }
        }
    }

    private void handleSessionAction(StudySession session, boolean isCurrentlyRegistered) {
        try {
            boolean success;
            if (isCurrentlyRegistered) {
                success = sessionFacade.leaveStudySession(session.getId());
                if (success) {
                    System.out.println("Left session: " + session.getTitle());
                    showInfo("Successfully left the session");
                } else {
                    showError("Failed to leave the session");
                }
            } else {
                success = sessionFacade.joinStudySession(session.getId());
                if (success) {
                    System.out.println("Joined session: " + session.getTitle());
                    showInfo("Successfully joined the session");
                } else {
                    showError("Failed to join the session (may be full)");
                }
            }

            if (success) {
                refreshSessions();
            }
        } catch (UnauthorizedException e) {
            showError("You must be logged in to perform this action");
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
