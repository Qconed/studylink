package dev.studylink.studylink.ui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import dev.studylink.studylink.business.Post;
import dev.studylink.studylink.business.PostComment;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.PostDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLPostDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class PostItemController {

    @FXML
    private Label authorLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private Label likesCountLabel;

    @FXML
    private Label commentsCountLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label likeIconLabel;
    
    @FXML
    private VBox commentsSection;
    
    @FXML
    private VBox commentsListContainer;
    
    @FXML
    private TextField commentInputField;
    
    @FXML
    private Button submitCommentButton;
    
    @FXML
    private Button deleteButton;
    
    private Post post;
    private boolean isLiked = false;
    private boolean commentsVisible = false;
    private PostDAO postDAO = MySQLPostDAO.getInstance();
    private User currentUser = SessionFacade.getInstance().getCurrentUser();

    /**
     * Cette méthode est appelée par le DashboardController 
     * pour remplir la bulle avec les vraies infos.
     */
    public void setPostData(Post post) {
        this.post = post;
        authorLabel.setText(post.getAuthorName());
        contentLabel.setText(post.getContent());
        likesCountLabel.setText(String.valueOf(post.getLikes()));
        commentsCountLabel.setText(String.valueOf(post.getCommentsCount()));
        dateLabel.setText(formatRelativeTime(post.getCreatedAt()));
        
        // Vérifier si l'utilisateur a déjà liké ce post
        if (currentUser != null && postDAO.hasUserLiked(post.getId(), currentUser.getId())) {
            isLiked = true;
            likeIconLabel.setStyle("-fx-text-fill: #FF0000;");
        } else {
            isLiked = false;
            likeIconLabel.setStyle("");
        }
        
        // Afficher le bouton de suppression si l'utilisateur est l'auteur ou admin
        if (currentUser != null) {
            boolean isAuthor = post.getUserId() == currentUser.getId();
            boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
            
            if (isAuthor || isAdmin) {
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
            }
        }
    }
    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Récemment";
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "À l'instant";
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            return "Il y a " + minutes + (minutes == 1 ? " minute" : " minutes");
        }
        
        long hours = seconds / 3600;
        if (hours < 24) {
            return "Il y a " + hours + (hours == 1 ? " heure" : " heures");
        }
        
        long days = seconds / 86400;
        return "Il y a " + days + (days == 1 ? " jour" : " jours");
    }

    @FXML
    private void onLikeClick() {
        if (post == null || currentUser == null) return;
        
        // Toggle le like (ajouter ou retirer)
        boolean success = postDAO.toggleLike(post.getId(), currentUser.getId());
        
        if (success) {
            isLiked = !isLiked;
            
            if (isLiked) {
                // Like ajouté : cœur rouge
                likeIconLabel.setStyle("-fx-text-fill: #FF0000;");
                int currentLikes = Integer.parseInt(likesCountLabel.getText());
                likesCountLabel.setText(String.valueOf(currentLikes + 1));
            } else {
                // Like retiré : cœur normal
                likeIconLabel.setStyle("");
                int currentLikes = Integer.parseInt(likesCountLabel.getText());
                likesCountLabel.setText(String.valueOf(currentLikes - 1));
            }
        }
    }

    @FXML
    private void onCommentClick() {
        if (post == null) return;
        
        commentsVisible = !commentsVisible;
        commentsSection.setVisible(commentsVisible);
        commentsSection.setManaged(commentsVisible);
        
        if (commentsVisible) {
            loadComments();
        }
    }
    
    @FXML
    private void onSubmitCommentClick() {
        String content = commentInputField.getText().trim();
        if (content.isEmpty() || post == null || currentUser == null) return;
        
        boolean success = postDAO.createComment(post.getId(), currentUser.getId(), content);
        
        if (success) {
            commentInputField.clear();
            loadComments();
            // Mettre à jour le compteur
            int currentCount = Integer.parseInt(commentsCountLabel.getText());
            commentsCountLabel.setText(String.valueOf(currentCount + 1));
        }
    }
    
    private void loadComments() {
        commentsListContainer.getChildren().clear();
        List<PostComment> comments = postDAO.getCommentsByPost(post.getId());
        
        if (comments.isEmpty()) {
            Label emptyLabel = new Label("Aucun commentaire pour le moment");
            emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
            commentsListContainer.getChildren().add(emptyLabel);
        } else {
            for (PostComment comment : comments) {
                VBox commentBox = createCommentBox(comment);
                commentsListContainer.getChildren().add(commentBox);
            }
        }
    }
    
    private VBox createCommentBox(PostComment comment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8;");
        
        // Auteur et date
        HBox header = new HBox(10);
        Label authorLabel = new Label(comment.getAuthorName());
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 12;");
        
        Label dateLabel = new Label(formatRelativeTime(comment.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11;");
        
        header.getChildren().addAll(authorLabel, dateLabel);
        
        // Contenu
        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 12;");
        
        box.getChildren().addAll(header, contentLabel);
        VBox.setMargin(box, new Insets(5, 0, 5, 0));
        
        return box;
    }
    
    @FXML
    private void onDeleteClick() {
        if (post == null || currentUser == null) return;
        
        // Vérifier les permissions
        boolean isAuthor = post.getUserId() == currentUser.getId();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        
        if (!isAuthor && !isAdmin) {
            System.out.println("Vous n'avez pas la permission de supprimer ce post");
            return;
        }
        
        // Supprimer le post
        boolean success = postDAO.deletePost(post.getId());
        
        if (success) {
            // Recharger le dashboard
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/studylink/studylink/dashboard-content.fxml"));
                ScrollPane newContent = loader.load();
                
                // Trouver le BorderPane principal et remplacer son contenu
                Node root = deleteButton.getScene().getRoot();
                if (root instanceof BorderPane) {
                    BorderPane borderPane = (BorderPane) root;
                    Node center = borderPane.getCenter();
                    if (center instanceof BorderPane) {
                        ((BorderPane) center).setCenter(newContent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erreur lors du rechargement du dashboard: " + e.getMessage());
            }
        } else {
            System.out.println("Erreur lors de la suppression du post");
        }
    }
}