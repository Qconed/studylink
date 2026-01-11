
package dev.studylink.studylink.dao;

import java.util.List;

import dev.studylink.studylink.business.Post;

public interface PostDAO {
    // Créer un post (la zone "Share your thoughts")
    boolean createPost(int userId, String content);
    
    // Récupérer tous les posts pour le Dashboard
    List<Post> getAllPosts();
    
    // Gérer les interactions sociales
    boolean incrementLike(int postId);
    
    void close();
}