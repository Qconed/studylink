package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.TutorProfile;
import dev.studylink.studylink.business.TutorSession;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.dao.TutorDAO;
import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.impl.db.mysql.MySQLTutorDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TutorMarketplaceController {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> subjectFilter;
    
    @FXML
    private Button allTutorsButton;
    
    @FXML
    private Button premiumOnlyButton;
    
    @FXML
    private Button createSessionButton;
    
    @FXML
    private VBox tutorsContainer;
    
    @FXML
    private VBox emptyState;
    
    private TutorDAO tutorDAO;
    private CategoryDAO categoryDAO;
    private User currentUser;
    private List<TutorProfile> allTutors;
    private List<TutorSession> allSessions = new ArrayList<>();
    
    @FXML
    public void initialize() {
        tutorDAO = MySQLTutorDAO.getInstance();
        UserFactory factory = MySQLUserFactory.getInstance();
        categoryDAO = factory.createCategoryDAO();
        currentUser = SessionFacade.getInstance().getCurrentUser();
        
        // Show create session button only for tutors
        if (currentUser != null && currentUser.getRole() == Role.TUTOR) {
            createSessionButton.setVisible(true);
            createSessionButton.setManaged(true);
        }
        
        // Populate subject filter from categories
        populateSubjectFilter();
        
        // Load all tutors first
        allTutors = tutorDAO.getAllTutorProfiles();
        
        // Then load all sessions
        loadAllSessions();
        
        // Finally display everything
        displayContent();
    }
    
    private void populateSubjectFilter() {
        // Load categories from database
        List<Category> categories = categoryDAO.getAllCategories();
        
        // Add "All Subjects" first
        subjectFilter.getItems().add("All Subjects");
        
        // Add category titles
        categories.stream()
            .map(Category::getTitle)
            .sorted()
            .forEach(title -> subjectFilter.getItems().add(title));
        
        subjectFilter.getSelectionModel().selectFirst();
    }
    
    private void loadAllSessions() {
        allSessions.clear();
        // Load sessions for all tutors
        if (allTutors != null && !allTutors.isEmpty()) {
            for (TutorProfile tutor : allTutors) {
                List<TutorSession> sessions = tutorDAO.getSessionsByTutorId(tutor.getId());
                allSessions.addAll(sessions);
            }
        }
    }
    
    private void displayContent() {
        displayTutors(allTutors);
        displaySessions(allSessions);
    }
    
    @FXML
    private void onSearchClick() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedSubject = subjectFilter.getValue();
        
        if (searchText.isEmpty() && (selectedSubject == null || selectedSubject.equals("All Subjects"))) {
            displayTutors(allTutors);
            return;
        }
        
        List<TutorProfile> filtered = allTutors.stream()
            .filter(tutor -> {
                boolean matchesSearch = searchText.isEmpty() || 
                    tutor.getTutorName().toLowerCase().contains(searchText) ||
                    tutor.getBio().toLowerCase().contains(searchText) ||
                    tutor.getSubjectsAsString().toLowerCase().contains(searchText);
                
                boolean matchesSubject = selectedSubject == null || 
                    selectedSubject.equals("All Subjects") ||
                    tutor.getSubjects().contains(selectedSubject);
                
                return matchesSearch && matchesSubject;
            })
            .toList();
        
        displayTutors(filtered);
    }
    
    @FXML
    private void onShowAllTutors() {
        displayTutors(allTutors);
        setActiveFilter(allTutorsButton);
    }
    
    @FXML
    private void onShowPremiumOnly() {
        List<TutorProfile> premiumTutors = tutorDAO.getPremiumTutors();
        displayTutors(premiumTutors);
        setActiveFilter(premiumOnlyButton);
    }
    
    private void setActiveFilter(Button activeButton) {
        // Reset all buttons
        allTutorsButton.setStyle("-fx-padding: 8 16; -fx-font-size: 13;");
        premiumOnlyButton.setStyle("-fx-padding: 8 16; -fx-font-size: 13;");
        
        // Set active style
        activeButton.setStyle("-fx-padding: 8 16; -fx-font-size: 13; -fx-background-color: #356EE9; -fx-text-fill: white;");
    }
    
    private void displayTutors(List<TutorProfile> tutors) {
        tutorsContainer.getChildren().clear();
        
        if (tutors.isEmpty() && allSessions.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }
        
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        
        // Add section header for tutors if there are any
        if (!tutors.isEmpty()) {
            Label tutorsHeader = new Label("Available Tutors");
            tutorsHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-padding: 10 0;");
            tutorsContainer.getChildren().add(tutorsHeader);
            
            for (TutorProfile tutor : tutors) {
                tutorsContainer.getChildren().add(createTutorCard(tutor));
            }
        }
    }
    
    private void displaySessions(List<TutorSession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        
        // Add section header for sessions
        Label sessionsHeader = new Label("Available Sessions");
        sessionsHeader.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-padding: 20 0 10 0;");
        tutorsContainer.getChildren().add(sessionsHeader);
        
        // Create cards for each session
        for (TutorSession session : sessions) {
            tutorsContainer.getChildren().add(createSessionCard(session));
        }
    }
    
    private VBox createTutorCard(TutorProfile tutor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("content-card");
        card.setStyle(card.getStyle() + "; -fx-padding: 25;");
        
        // Header Row: Name, Premium Badge, Stats
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Name and Subject
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(tutor.getTutorName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        Label subjectLabel = new Label(tutor.getSubjects().isEmpty() ? "General Tutoring" : tutor.getSubjects().get(0));
        subjectLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14;");
        
        nameBox.getChildren().addAll(nameLabel, subjectLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Premium Badge
        if (tutor.isPremium()) {
            Label premiumBadge = new Label("⭐ Premium");
            premiumBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold;");
            headerRow.getChildren().addAll(nameBox, spacer, premiumBadge);
        } else {
            headerRow.getChildren().addAll(nameBox, spacer);
        }
        
        // Bio
        Label bioLabel = new Label(tutor.getBio() != null ? tutor.getBio() : "Experienced tutor ready to help you succeed!");
        bioLabel.setWrapText(true);
        bioLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 14;");
        
        // Stats Row
        HBox statsRow = new HBox(25);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        
        // Rating
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label(String.format("⭐ %.1f", tutor.getAverageRating()));
        ratingLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        int reviewCount = tutorDAO.getReviewCount(tutor.getId());
        Label reviewCountLabel = new Label(String.format("(%d reviews)", reviewCount));
        reviewCountLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        ratingBox.getChildren().addAll(ratingLabel, reviewCountLabel);
        
        // Sessions Count
        Label sessionsLabel = new Label(String.format("🎓 %d sessions", tutor.getTotalSessions()));
        sessionsLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        statsRow.getChildren().addAll(ratingBox, new Separator(javafx.geometry.Orientation.VERTICAL), sessionsLabel);
        
        // Subjects Tags
        HBox tagsRow = new HBox(8);
        tagsRow.setAlignment(Pos.CENTER_LEFT);
        
        int maxTags = Math.min(3, tutor.getSubjects().size());
        for (int i = 0; i < maxTags; i++) {
            Label tag = new Label(tutor.getSubjects().get(i));
            tag.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1E40AF; -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 12;");
            tagsRow.getChildren().add(tag);
        }
        
        // Availability and Price Row
        HBox bottomRow = new HBox(20);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        // Availability
        Label availabilityLabel = new Label("🕐 " + (tutor.getAvailability() != null ? tutor.getAvailability() : "Flexible schedule"));
        availabilityLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        
        // Price
        Label priceLabel = new Label(String.format("$%.0f/hour", tutor.getHourlyRate()));
        priceLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #10B981;");
        
        bottomRow.getChildren().addAll(availabilityLabel, spacer2, priceLabel);
        
        // Action Buttons
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        
        Button bookButton = new Button("Book Session");
        bookButton.getStyleClass().add("primary-button");
        bookButton.setStyle(bookButton.getStyle() + "; -fx-padding: 10 20;");
        bookButton.setOnAction(e -> onBookSession(tutor));
        
        Button viewProfileButton = new Button("View Profile");
        viewProfileButton.getStyleClass().add("secondary-button");
        viewProfileButton.setStyle(viewProfileButton.getStyle() + "; -fx-padding: 10 20;");
        viewProfileButton.setOnAction(e -> onViewProfile(tutor));
        
        actionsRow.getChildren().addAll(bookButton, viewProfileButton);
        
        // Add separator before buttons
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        
        card.getChildren().addAll(headerRow, bioLabel, statsRow, tagsRow, bottomRow, separator, actionsRow);
        
        return card;
    }
    
    private VBox createSessionCard(TutorSession session) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        // Get tutor name for the session
        String tutorName = "Unknown Tutor";
        var tutorOpt = tutorDAO.getTutorProfileById(session.getTutorId());
        if (tutorOpt.isPresent()) {
            tutorName = tutorOpt.get().getTutorName();
        }
        
        // Header Row: Title and Subject
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(session.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        Label subjectBadge = new Label(session.getSubject());
        subjectBadge.setStyle("-fx-background-color: #356EE9; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 12;");
        
        headerRow.getChildren().addAll(titleLabel, subjectBadge);
        
        // Tutor name
        Label tutorLabel = new Label("👨‍🏫 By " + tutorName);
        tutorLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14;");
        
        // Description
        Label descriptionLabel = new Label(session.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(600);
        descriptionLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 14;");
        
        // Info Row: Duration, Price, Max Students
        HBox infoRow = new HBox(25);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        Label durationLabel = new Label("⏱️ " + session.getFormattedDuration());
        durationLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        Label priceLabel = new Label("💵 " + session.getFormattedPrice());
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669; -fx-font-size: 15;");
        
        Label maxStudentsLabel = new Label("👥 Max " + session.getMaxStudents() + " students");
        maxStudentsLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        infoRow.getChildren().addAll(durationLabel, priceLabel, maxStudentsLabel);
        
        // Action Button
        Button bookButton = new Button("Book This Session");
        bookButton.setStyle("-fx-background-color: #356EE9; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        bookButton.setOnAction(e -> onBookSessionClick(session));
        
        HBox buttonRow = new HBox(bookButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        
        // Add separator before button
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        
        card.getChildren().addAll(headerRow, tutorLabel, descriptionLabel, infoRow, separator, buttonRow);
        
        return card;
    }
    
    private void onBookSessionClick(TutorSession session) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in to add a session to cart.");
            return;
        }
        
        // Prevent tutors from booking their own sessions
        var tutorProfileOpt = tutorDAO.getTutorProfileById(session.getTutorId());
        if (tutorProfileOpt.isPresent()) {
            TutorProfile tutorProfile = tutorProfileOpt.get();
            if (tutorProfile.getUserId() == currentUser.getId()) {
                showAlert(Alert.AlertType.WARNING, "Cannot Add", "You cannot add your own tutoring session to cart.");
                return;
            }
        }
        
        // Add session to cart (exactly like resources)
        UserFactory factory = MySQLUserFactory.getInstance();
        var cartDAO = factory.createCartDAO();
        
        // Sessions always have quantity = 1
        boolean success = cartDAO.addSessionToCart(currentUser.getId(), session.getId(), 1);
        
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Added to Cart");
            alert.setHeaderText("Session Added Successfully");
            alert.setContentText(String.format(
                "Added to cart:\n\n" +
                "Session: %s\n" +
                "Subject: %s\n" +
                "Duration: %s\n" +
                "Price: %s\n\n" +
                "Go to your cart to complete the booking.",
                session.getTitle(),
                session.getSubject(),
                session.getFormattedDuration(),
                session.getFormattedPrice()
            ));
            alert.showAndWait();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to Add", 
                     "Failed to add session to cart. It may already be in your cart.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void onBookSession(TutorProfile tutor) {
        // TODO: Open booking dialog or navigate to booking page
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Session");
        alert.setHeaderText("Book with " + tutor.getTutorName());
        alert.setContentText("Session booking functionality will be implemented soon!");
        alert.showAndWait();
    }
    
    private void onViewProfile(TutorProfile tutor) {
        try {
            // Get the User object for this tutor
            UserFactory factory = MySQLUserFactory.getInstance();
            var userDAO = factory.createUserDAO();
            Optional<User> tutorUserOpt = userDAO.findById(tutor.getUserId());
            
            if (tutorUserOpt.isPresent()) {
                // Load user profile view (same as in friends section)
                UserProfileController.setUserToLoad(tutorUserOpt.get());
                MainAppController.loadContentStatic("/dev/studylink/studylink/user-profile-view.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load tutor profile.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load tutor profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onCreateSession() {
        System.out.println("Create Session button clicked in marketplace");
        MainAppController.loadContentStatic("/dev/studylink/studylink/create-tutor-session-view.fxml");
    }
}
