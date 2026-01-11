package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.*;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.exception.UnauthorizedException;
import dev.studylink.studylink.impl.db.mysql.MySQLCategoryDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CreateSessionDialogController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ListView<Category> categoriesList;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> startHourCombo;

    @FXML
    private ComboBox<String> startMinuteCombo;

    @FXML
    private ComboBox<String> endHourCombo;

    @FXML
    private ComboBox<String> endMinuteCombo;

    @FXML
    private Spinner<Integer> minParticipantsSpinner;

    @FXML
    private Spinner<Integer> maxParticipantsSpinner;

    @FXML
    private TextField locationField;

    @FXML
    private CheckBox tutoredCheckBox;

    @FXML
    private VBox priceBox;

    @FXML
    private TextField priceField;

    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private StudySessionController parentController;
    private CategoryDAO categoryDAO;

    @FXML
    public void initialize() {
        // Initialize time combos
        initializeTimeCombos();

        // Initialize spinners
        minParticipantsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );
        maxParticipantsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10)
        );

        // Load categories
        loadCategories();

        // Set multiple selection for categories
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add listener to tutored checkbox
        tutoredCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            priceBox.setVisible(newVal);
            priceBox.setManaged(newVal);
            if (!newVal) {
                priceField.setText("0");
            }
        });

        // Set default price
        priceField.setText("0");

        // Set default date to today
        datePicker.setValue(LocalDate.now());
    }

    private void initializeTimeCombos() {
        // Hours (00-23)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        startHourCombo.setItems(hours);
        endHourCombo.setItems(hours);

        // Minutes (00, 15, 30, 45)
        ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45");
        startMinuteCombo.setItems(minutes);
        endMinuteCombo.setItems(minutes);

        // Set default values
        startHourCombo.setValue("09");
        startMinuteCombo.setValue("00");
        endHourCombo.setValue("11");
        endMinuteCombo.setValue("00");
    }

    private void loadCategories() {
        try {
            categoryDAO = MySQLCategoryDAO.getInstance();
            List<Category> allCategories = categoryDAO.getAllCategories();
            
            // Use custom cell factory to display category info
            categoriesList.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle() + " (" + item.getType() + ")");
                    }
                }
            });
            
            categoriesList.getItems().addAll(allCategories);
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(StudySessionController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleCreate() {
        errorLabel.setVisible(false);

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Create StudySession object
            StudySession session = new StudySession();
            session.setTitle(titleField.getText().trim());
            session.setDescription(descriptionField.getText().trim());

            // Set tutored and price
            boolean isTutored = tutoredCheckBox.isSelected();
            session.setTutored(isTutored);

            if (isTutored) {
                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    session.setPrice(price);
                } catch (NumberFormatException e) {
                    showError("Invalid price format");
                    return;
                }
            } else {
                session.setPrice(0.0);
            }

            // Set participants
            session.setMinParticipants(minParticipantsSpinner.getValue());
            session.setMaxParticipants(maxParticipantsSpinner.getValue());

            // Create TimeSlot
            LocalDate date = datePicker.getValue();
            int startHour = Integer.parseInt(startHourCombo.getValue());
            int startMinute = Integer.parseInt(startMinuteCombo.getValue());
            int endHour = Integer.parseInt(endHourCombo.getValue());
            int endMinute = Integer.parseInt(endMinuteCombo.getValue());

            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(startHour, startMinute));
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.of(endHour, endMinute));

            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setStartTime(startTime);
            timeSlot.setEndTime(endTime);
            timeSlot.setDateOfDay(startTime);
            
            String location = locationField.getText().trim();
            if (!location.isEmpty()) {
                timeSlot.setLocation(location);
            }
            
            session.setTimeSlot(timeSlot);

            // Set categories
            List<Category> selectedCategories = categoriesList.getSelectionModel().getSelectedItems();
            List<Integer> categoryIds = new ArrayList<>();
            for (Category cat : selectedCategories) {
                categoryIds.add(cat.getId());
            }
            session.setCategoryIds(categoryIds);

            // Validate the session
            if (!session.validate()) {
                showError("Invalid session data. Please check all fields.");
                return;
            }

            // Create the session using facade
            StudySessionFacade facade = StudySessionFacade.getInstance();
            StudySession createdSession = facade.createStudySession(session);

            System.out.println("\n✓ Study session created successfully!");
            System.out.println("  ID: " + createdSession.getId());
            System.out.println("  Title: " + createdSession.getTitle());
            System.out.println("  Date: " + createdSession.getTimeSlot().getStartTime());

            // Refresh parent view
            if (parentController != null) {
                parentController.refreshSessions();
            }

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Study session created successfully!");
            alert.showAndWait();

            // Close dialog
            dialogStage.close();

        } catch (UnauthorizedException e) {
            showError("You must be logged in to create a session");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error creating session: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        // Check title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showError("Please enter a title");
            return false;
        }

        // Check description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            showError("Please enter a description");
            return false;
        }

        // Check categories
        if (categoriesList.getSelectionModel().getSelectedItems().isEmpty()) {
            showError("Please select at least one category");
            return false;
        }

        // Check date
        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return false;
        }

        // Check time
        if (startHourCombo.getValue() == null || startMinuteCombo.getValue() == null ||
            endHourCombo.getValue() == null || endMinuteCombo.getValue() == null) {
            showError("Please select start and end times");
            return false;
        }

        // Validate time logic
        int startHour = Integer.parseInt(startHourCombo.getValue());
        int startMinute = Integer.parseInt(startMinuteCombo.getValue());
        int endHour = Integer.parseInt(endHourCombo.getValue());
        int endMinute = Integer.parseInt(endMinuteCombo.getValue());

        LocalTime startTime = LocalTime.of(startHour, startMinute);
        LocalTime endTime = LocalTime.of(endHour, endMinute);

        if (!endTime.isAfter(startTime)) {
            showError("End time must be after start time");
            return false;
        }

        // Check participants
        if (minParticipantsSpinner.getValue() > maxParticipantsSpinner.getValue()) {
            showError("Max participants must be greater than or equal to min participants");
            return false;
        }

        // Check price if tutored
        if (tutoredCheckBox.isSelected()) {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    showError("Price cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Invalid price format");
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void cleanup() {
        if (categoryDAO != null) {
            categoryDAO.close();
        }
    }
}
