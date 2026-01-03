package dev.studylink.studylink.db;

import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Initialise la base de données avec les données de l'admin
 */
public class DatabaseInitializer {

    private static final String ADMIN_EMAIL = "admin@studylink.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_FULLNAME = "Admin User";

    private final UserDAO userDAO;
    private final CategoryDAO categoryDAO;

    public DatabaseInitializer() {
        MySQLUserFactory factory = MySQLUserFactory.getInstance();
        this.userDAO = factory.createUserDAO();
        this.categoryDAO = factory.createCategoryDAO();
    }

    /**
     * Initialise toutes les données de base
     */
    public void initialize() {
        System.out.println("=== Initialisation de la base de données ===\n");

        createAdminIfNotExists();
        createDefaultCategories();

        System.out.println("\n=== Initialisation terminée ===");
    }

    /**
     * Crée le compte admin s'il n'existe pas
     */
    private void createAdminIfNotExists() {
        System.out.println("Vérification du compte administrateur...");

        if (userDAO.findByEmail(ADMIN_EMAIL).isPresent()) {
            System.out.println("✅ Compte admin existe déjà: " + ADMIN_EMAIL);
            return;
        }

        System.out.println("Création du compte admin...");
        String passwordHash = BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt());

        // Créer l'utilisateur
        boolean created = userDAO.createUser(ADMIN_FULLNAME, ADMIN_EMAIL, passwordHash);

        if (created) {
            // Récupérer l'utilisateur créé et le promouvoir en ADMIN
            userDAO.findByEmail(ADMIN_EMAIL).ifPresent(user -> {
                userDAO.updateUserRole(user.getId(), Role.ADMIN);
                System.out.println("✅ Compte admin créé avec succès!");
                System.out.println("   Email: " + ADMIN_EMAIL);
                System.out.println("   Password: " + ADMIN_PASSWORD);
                System.out.println("   Role: ADMIN");
            });
        } else {
            System.err.println("❌ Erreur lors de la création du compte admin");
        }
    }

    /**
     * Crée les catégories par défaut si elles n'existent pas
     */
    private void createDefaultCategories() {
        System.out.println("\nVérification des catégories par défaut...");

        if (!categoryDAO.getAllCategories().isEmpty()) {
            System.out.println("✅ Catégories existent déjà (" +
                    categoryDAO.getAllCategories().size() + " catégories)");
            return;
        }

        System.out.println("Création des catégories par défaut...");

        // Academic subjects
        createCategory("Mathematics", "Mathematical sciences and applications",
                CategoryType.ACADEMIC_SUBJECT, 2);
        createCategory("Computer Science", "Programming, algorithms, and software development",
                CategoryType.ACADEMIC_SUBJECT, 3);
        createCategory("Physics", "Physical sciences and engineering",
                CategoryType.ACADEMIC_SUBJECT, 2);
        createCategory("Literature", "Literary studies and analysis",
                CategoryType.ACADEMIC_SUBJECT, 1);
        createCategory("Chemistry", "Chemical sciences and laboratory work",
                CategoryType.ACADEMIC_SUBJECT, 2);
        createCategory("Biology", "Life sciences and biotechnology",
                CategoryType.ACADEMIC_SUBJECT, 2);

        // Personal interests
        createCategory("Music", "Musical instruments and theory",
                CategoryType.PERSONAL_INTEREST, 0);
        createCategory("Sports", "Physical activities and athletics",
                CategoryType.PERSONAL_INTEREST, 0);
        createCategory("Cooking", "Culinary arts and recipes",
                CategoryType.PERSONAL_INTEREST, 0);
        createCategory("Photography", "Photo techniques and editing",
                CategoryType.PERSONAL_INTEREST, 0);
        createCategory("Gaming", "Video games and esports",
                CategoryType.PERSONAL_INTEREST, 0);

        // Training/Schools
        createCategory("Polytech", "Engineering school",
                CategoryType.TRAINING, 0);
        createCategory("INSA", "Applied sciences institute",
                CategoryType.TRAINING, 0);
        createCategory("Master Physics", "Graduate physics program",
                CategoryType.TRAINING, 0);
        createCategory("Master Computer Science", "Graduate CS program",
                CategoryType.TRAINING, 0);

        System.out.println("✅ Catégories créées avec succès!");
    }

    private void createCategory(String title, String description, CategoryType type, int level) {
        if (categoryDAO.createCategory(title, description, type, level)) {
            System.out.println("   ✓ " + title);
        } else {
            System.err.println("   ✗ Erreur: " + title);
        }
    }

    /**
     * Vérifie et affiche l'état de la base de données
     */
    public void checkDatabaseStatus() {
        System.out.println("\n=== État de la base de données ===");

        try (java.sql.Connection conn = Connection.getDataSource().getConnection()) {

            // Vérifier la table users
            String userCountQuery = "SELECT COUNT(*) as count FROM users";
            PreparedStatement stmt = conn.prepareStatement(userCountQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Utilisateurs: " + rs.getInt("count"));
            }

            // Vérifier la table categories
            String catCountQuery = "SELECT COUNT(*) as count FROM categories";
            stmt = conn.prepareStatement(catCountQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Catégories: " + rs.getInt("count"));
            }

            // Vérifier la table friendships
            String friendCountQuery = "SELECT COUNT(*) as count FROM friendships";
            stmt = conn.prepareStatement(friendCountQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Amitiés: " + rs.getInt("count"));
            }

            // Vérifier si l'admin existe
            String adminQuery = "SELECT * FROM users WHERE role = 'ADMIN'";
            stmt = conn.prepareStatement(adminQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("\n✅ Admin existant:");
                System.out.println("   Email: " + rs.getString("email"));
                System.out.println("   Name: " + rs.getString("fullname"));
            } else {
                System.out.println("\n⚠️  Aucun admin trouvé!");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
        }

        System.out.println("================================\n");
    }

    public static void main(String[] args) {
        DatabaseInitializer initializer = new DatabaseInitializer();

        // Afficher l'état actuel
        initializer.checkDatabaseStatus();

        // Initialiser
        initializer.initialize();

        // Afficher l'état après initialisation
        initializer.checkDatabaseStatus();
    }
}