package dev.studylink.studylink;

import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;

import java.util.Optional;

public class TestDB {
    public static void main(String[] args) {
        UserDAO userDAO = MySQLUserFactory.getInstance().createUserDAO();
        
        System.out.println("=== Test de connexion à la base de données ===\n");
        
        // Test 1: Login avec l'utilisateur admin (password: test123)
        System.out.println("Test 1: Login avec admin/test123");
        Optional<User> user = userDAO.findByCredentials("admin", "test123");
        
        if (user.isPresent()) {
            System.out.println("✅ Login réussi!");
            System.out.println(user.get());
        } else {
            System.out.println("❌ Login échoué");
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 2: Recherche par username
        System.out.println("Test 2: Recherche de l'utilisateur 'admin'");
        Optional<User> foundUser = userDAO.findByUsername("admin");
        
        if (foundUser.isPresent()) {
            System.out.println("✅ Utilisateur trouvé!");
            System.out.println(foundUser.get());
        } else {
            System.out.println("❌ Utilisateur non trouvé");
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 3: Mauvais mot de passe
        System.out.println("Test 3: Login avec mauvais mot de passe");
        Optional<User> badLogin = userDAO.findByCredentials("admin", "wrongpassword");
        
        if (badLogin.isPresent()) {
            System.out.println("❌ ERREUR: Login ne devrait pas réussir!");
        } else {
            System.out.println("✅ Login correctement refusé");
        }
        
        userDAO.close();
        System.out.println("\n=== Tests terminés ===");
    }
}
