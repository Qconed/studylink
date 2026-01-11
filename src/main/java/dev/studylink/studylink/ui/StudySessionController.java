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
        createdSessionsList.setCellFactory(lv -> new StudySessionCell(CellType.CREATED));
        registeredSessionsList.setCellFactory(lv -> new StudySessionCell(CellType.REGISTERED));
        recommendedSessionsList.setCellFactory(lv -> new StudySessionCell(CellType.RECOMMENDED));
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
        try {
            List<StudySession> registeredSessions = sessionFacade.listRegisteredSessions();
            registeredSessionsList.getItems().clear();
            registeredSessionsList.getItems().addAll(registeredSessions);
        } catch (UnauthorizedException e) {
            System.err.println("Error loading registered sessions: " + e.getMessage());
            registeredSessionsList.getItems().clear();
        } catch (Exception e) {
            System.err.println("Error loading registered sessions: " + e.getMessage());
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

    // Types de cellules pour adapter l'affichage
    private enum CellType {
        CREATED,      // Sessions créées par l'utilisateur
        REGISTERED,   // Sessions où l'utilisateur est inscrit
        RECOMMENDED   // Sessions recommandées
    }

    // Inner class for custom cell rendering
    private class StudySessionCell extends ListCell<StudySession> {
        private final CellType cellType;

        public StudySessionCell(CellType cellType) {
            this.cellType = cellType;
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

                // Add action button based on cell type
                if (cellType == CellType.CREATED) {
                    // Bouton Cancel pour les sessions créées
                    HBox buttonBox = new HBox();
                    buttonBox.setAlignment(Pos.CENTER_RIGHT);
                    buttonBox.setPadding(new Insets(5, 0, 0, 0));

                    Button cancelButton = new Button("Cancel Session");
                    cancelButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

                    cancelButton.setOnAction(e -> {
                        handleCancelSession(session);
                    });

                    buttonBox.getChildren().add(cancelButton);
                    content.getChildren().add(buttonBox);
                    
                } else if (cellType == CellType.REGISTERED) {
                    // Bouton Leave pour les sessions inscrites
                    HBox buttonBox = new HBox();
                    buttonBox.setAlignment(Pos.CENTER_RIGHT);
                    buttonBox.setPadding(new Insets(5, 0, 0, 0));

                    Button leaveButton = new Button("Leave Session");
                    leaveButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

                    leaveButton.setOnAction(e -> {
                        handleLeaveSession(session);
                    });

                    buttonBox.getChildren().add(leaveButton);
                    content.getChildren().add(buttonBox);
                    
                } else if (cellType == CellType.RECOMMENDED) {
                    // Bouton Join pour les sessions recommandées
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
                    // Afficher un message d'erreur plus détaillé
                    System.err.println("Failed to join session ID: " + session.getId());
                    showError("Failed to join the session. Please check the console for details.");
                }
            }

            if (success) {
                refreshSessions();
            }
        } catch (UnauthorizedException e) {
            showError("You must be logged in to perform this action");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLeaveSession(StudySession session) {
        try {
            // Confirm dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Leave Session");
            confirmAlert.setHeaderText("Leave " + session.getTitle() + "?");
            confirmAlert.setContentText("Are you sure you want to leave this session?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                boolean success = sessionFacade.leaveStudySession(session.getId());
                if (success) {
                    System.out.println("Left session: " + session.getTitle());
                    showInfo("Successfully left the session");
                    refreshSessions();
                } else {
                    showError("Failed to leave the session");
                }
            }
        } catch (UnauthorizedException e) {
            showError("You must be logged in to leave this session");
        } catch (Exception e) {
            showError("Error leaving session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCancelSession(StudySession session) {
        try {
            // Confirm dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Cancel Session");
            confirmAlert.setHeaderText("Cancel " + session.getTitle() + "?");
            confirmAlert.setContentText("Are you sure you want to cancel this session? This action cannot be undone.");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                boolean success = sessionFacade.cancelStudySession(session.getId());
                if (success) {
                    System.out.println("Cancelled session: " + session.getTitle());
                    showInfo("Session cancelled successfully");
                    refreshSessions();
                } else {
                    showError("Failed to cancel the session");
                }
            }
        } catch (UnauthorizedException e) {
            showError("You must be the organizer to cancel this session");
        } catch (Exception e) {
            showError("Error cancelling session: " + e.getMessage());
            e.printStackTrace();
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
