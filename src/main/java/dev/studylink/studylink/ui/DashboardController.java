package dev.studylink.studylink.ui;

import java.io.IOException;
import java.util.List;

import dev.studylink.studylink.business.Post;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.PostDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLPostDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
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
        
        // Sécurité si l'utilisateur n'est pas connecté
        if (currentUser != null) {
            dashboardWelcomeLabel.setText("Welcome back, " + currentUser.getFullname() + "!");
        }

        // CRUCIAL : Initialisez votre DAO ici pour éviter l'erreur de build
        this.postDAO = dev.studylink.studylink.impl.db.mysql.MySQLPostDAO.getInstance();
        
        loadFeed();
    }

    // Publier un nouveau post
    @FXML
    private void onPostSubmit() {
        String content = postInputField.getText();
        if (content != null && !content.isEmpty()) {
            postDAO.createPost(currentUser.getId(), content);
            postInputField.clear();
            loadFeed(); // Rafraîchir
        }
    }

    // Charger le flux social
    private void loadFeed() {
        postsFeedContainer.getChildren().clear();
        List<Post> posts = postDAO.getAllPosts();

        for (Post post : posts) {
            try {
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/post-item.fxml"));
                VBox postNode = loader.load();
                
                PostItemController controller = loader.getController();
                controller.setPostData(post);
                
                postsFeedContainer.getChildren().add(postNode);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}