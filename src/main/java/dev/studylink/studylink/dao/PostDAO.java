
package dev.studylink.studylink.dao;

import java.util.List;

import dev.studylink.studylink.business.Post;
import dev.studylink.studylink.business.PostComment;

public interface PostDAO {
    // Créer un post (la zone "Share your thoughts")
    boolean createPost(int userId, String content);
    
    // Récupérer tous les posts pour le Dashboard
    List<Post> getAllPosts();
    
    // Supprimer un post
    boolean deletePost(int postId);
    
    // Gérer les interactions sociales
    boolean incrementLike(int postId);
    boolean hasUserLiked(int postId, int userId);
    boolean toggleLike(int postId, int userId);
    int getLikesCount(int postId);
    
    // Gérer les commentaires
    boolean createComment(int postId, int userId, String content);
    List<PostComment> getCommentsByPost(int postId);
    int getCommentsCount(int postId);
    
    void close();
}