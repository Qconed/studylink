package dev.studylink.studylink.ui;

import java.io.IOException;
import java.util.List;

import dev.studylink.studylink.business.Post;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.PostDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLPostDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
public class DashboardController {
    @FXML private VBox postsFeedContainer; // La VBox centrale de votre dashboard
    @FXML private TextField postInputField;
    @FXML private Label dashboardWelcomeLabel;

    private PostDAO postDAO; 
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionFacade.getInstance().getCurrentUser();
        if (currentUser != null) {
            dashboardWelcomeLabel.setText("Welcome back, " + currentUser.getFullname() + "!");
        }

        // 1. INITIALISATION DU DAO (Évite le NullPointerException)
        this.postDAO = dev.studylink.studylink.impl.db.mysql.MySQLPostDAO.getInstance();
        
        loadFeed();
    }
    @FXML
    public void onPostSubmit(ActionEvent event) {
        String content = postInputField.getText();
        if (content != null && !content.isEmpty()) {
            postDAO.createPost(currentUser.getId(), content);
            postInputField.clear();
            loadFeed(); 
        }
    }
    private void loadFeed() {
        postsFeedContainer.getChildren().clear();
        List<Post> posts = postDAO.getAllPosts();

        for (Post post : posts) {
            try {
                // 2. CHEMIN ABSOLU VERS LE FXML (Évite l'erreur de chargement)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/post-item.fxml"));
                VBox postNode = loader.load();
                
                PostItemController controller = loader.getController();
                controller.setPostData(post);
                
                postsFeedContainer.getChildren().add(postNode);
            } catch (IOException e) { 
                System.err.println("Erreur chargement post-item: " + e.getMessage());
                e.printStackTrace(); 
            }
        }

    }
}