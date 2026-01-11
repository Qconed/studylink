package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PostItemController {

    @FXML
    private Label authorLabel;

    @FXML
    private Label contentLabel;

    @FXML
    private Label likesCountLabel;

    @FXML
    private Label commentsCountLabel;

    /**
     * Cette méthode est appelée par le DashboardController 
     * pour remplir la bulle avec les vraies infos.
     */
    public void setPostData(Post post) {
        authorLabel.setText(post.getAuthorName());
        contentLabel.setText(post.getContent());
        likesCountLabel.setText(String.valueOf(post.getLikes()));
        commentsCountLabel.setText(String.valueOf(post.getCommentsCount()));
    }

    @FXML
    private void onLikeClick() {
        // Ici tu appelleras ton futur PostDAO pour incrémenter les likes
        System.out.println("Like cliqué !");
    }

    @FXML
    private void onCommentClick() {
        // Ici tu utiliseras ton CommentDAO pour afficher les commentaires
        //
        System.out.println("Voir les commentaires");
    }
}