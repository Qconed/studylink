package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import dev.studylink.studylink.dao.*;
import dev.studylink.studylink.impl.db.mysql.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class CreateTutorSessionController {
    @FXML private TextField titleField;
    @FXML private ComboBox<String> subjectComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField durationField;
    @FXML private TextField priceField;
    @FXML private TextField maxStudentsField;

    private TutorDAO tutorDAO;
    private CategoryDAO categoryDAO;
    private User currentUser;
    private TutorProfile tutorProfile;

    @FXML
    public void initialize() {
        System.out.println("CreateTutorSessionController - Initialize called");
        
        tutorDAO = MySQLTutorDAO.getInstance();
        categoryDAO = MySQLUserFactory.getInstance().createCategoryDAO();
        currentUser = SessionFacade.getInstance().getCurrentUser();

        if (currentUser == null) {
            System.err.println("ERROR: No current user found!");
            showAlert(Alert.AlertType.ERROR, "Accès Refusé", "Vous devez être connecté.");
            return;
        }
        
        System.out.println("Current user: " + currentUser.getFullname() + " (Role: " + currentUser.getRole() + ")");

        // Vérifier si l'utilisateur est tuteur
        if (currentUser.getRole() != Role.TUTOR && currentUser.getRole() != Role.ADMIN) {
            System.err.println("ERROR: User is not TUTOR or ADMIN");
            showAlert(Alert.AlertType.ERROR, "Accès Refusé", "Seuls les tuteurs peuvent créer des sessions.");
            goBackToMarketplace();
            return;
        }

        // Récupérer le profil tuteur
        var profileOpt = tutorDAO.getTutorProfileByUserId(currentUser.getId());
        if (profileOpt.isEmpty()) {
            System.out.println("No tutor profile found, creating default profile...");
            
            // Créer automatiquement un profil tuteur basique
            TutorProfile defaultProfile = new TutorProfile(
                currentUser.getId(),
                "New tutor profile - Please update your information",
                20.0, // Tarif horaire par défaut
                List.of("General"), // Sujet par défaut
                "Available on request" // Disponibilité par défaut
            );
            
            boolean created = tutorDAO.createTutorProfile(defaultProfile);
            if (!created) {
                System.err.println("ERROR: Failed to create default tutor profile");
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Impossible de créer votre profil tuteur. Veuillez contacter l'administrateur.");
                goBackToMarketplace();
                return;
            }
            
            System.out.println("Default tutor profile created successfully");
            
            // Récupérer le profil qui vient d'être créé
            profileOpt = tutorDAO.getTutorProfileByUserId(currentUser.getId());
            if (profileOpt.isEmpty()) {
                System.err.println("ERROR: Could not retrieve newly created profile");
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de la récupération du profil. Veuillez réessayer.");
                goBackToMarketplace();
                return;
            }
        }
        tutorProfile = profileOpt.get();
        System.out.println("Tutor profile loaded: ID=" + tutorProfile.getId());

        populateSubjects();
    }

    private void populateSubjects() {
        System.out.println("Populating subjects...");
        subjectComboBox.getItems().clear();
        
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            System.out.println("Categories loaded: " + (categories != null ? categories.size() : 0));
            
            if (categories != null && !categories.isEmpty()) {
                categories.stream()
                        .map(Category::getTitle)
                        .filter(title -> title != null && !title.trim().isEmpty())
                        .sorted()
                        .forEach(title -> {
                            System.out.println("  - Adding category: " + title);
                            subjectComboBox.getItems().add(title);
                        });
            }
        } catch (Exception e) {
            System.err.println("ERROR loading categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback si la table catégories est vide
        if (subjectComboBox.getItems().isEmpty()) {
            System.out.println("No categories in DB, using fallback subjects");
            subjectComboBox.getItems().addAll(
                "Computer Science", "Mathematics", "Physics", "Chemistry",
                "Biology", "English", "French", "History", "Economics"
            );
        }
        
        System.out.println("Total subjects available: " + subjectComboBox.getItems().size());
    }

    @FXML
    public void onCancel() {
        System.out.println("Cancel button clicked");
        goBackToMarketplace();
    }

    @FXML
    public void onCreate() {
        System.out.println("Create button clicked");
        
        if (!validateInputs()) {
            System.err.println("Validation failed");
            return;
        }

        try {
            String title = titleField.getText().trim();
            String subject = subjectComboBox.getValue();
            String description = descriptionArea.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int duration = Integer.parseInt(durationField.getText().trim());
            int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
            
            System.out.println("Creating session: " + title + " by tutor ID: " + tutorProfile.getId());

            TutorSession session = new TutorSession(
                tutorProfile.getId(),
                title,
                subject,
                description,
                price,
                duration,
                maxStudents
            );

            boolean created = tutorDAO.createTutorSession(session);
            System.out.println("Session creation result: " + created);
            
            if (created) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Session de tutorat créée avec succès !");
                goBackToMarketplace();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création de la session.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Number format error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vérifiez que les valeurs numériques sont valides.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue.");
        }
    }

    private boolean validateInputs() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer un titre.");
            return false;
        }
        
        if (subjectComboBox.getValue() == null || subjectComboBox.getValue().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un sujet.");
            return false;
        }
        
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer une description.");
            return false;
        }
        
        if (durationField.getText() == null || durationField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer la durée.");
            return false;
        }
        
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer le prix.");
            return false;
        }
        
        if (maxStudentsField.getText() == null || maxStudentsField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer le nombre maximum d'étudiants.");
            return false;
        }
        
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La durée doit être supérieure à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La durée doit être un nombre entier.");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le prix ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prix doit être un nombre valide.");
            return false;
        }
        
        try {
            int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
            if (maxStudents <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le nombre d'étudiants doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nombre d'étudiants doit être un nombre entier.");
            return false;
        }
        
        return true;
    }
    
    private void goBackToMarketplace() {
        System.out.println("Navigating back to marketplace...");
        MainAppController.loadContentStatic("/dev/studylink/studylink/tutor-marketplace.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}