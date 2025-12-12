
package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserDAO;

// Session facade will allow to the UI to easiliy use the business logic (for now there is only the user management)
// follows the singleton desing pattern
public class SessionFacade {
    private static SessionFacade instance = null;
    private UserManager userManager = UserManager.getInstance((UserFactory) MySQLUserFactory.getInstance()); // delegate for the user management. But only need to know the UserFactory, not the concrete implementation of it
    private final UserDAO userDAO = new MySQLUserDAO();

    // CONSTRUCTION -------------------
    private SessionFacade() {}

    // methode d'obtention d'instance singleton
    public static SessionFacade getInstance() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }

    // TODO will jave to delegate to the usermanager
    public boolean login(String email, String password) {
        // On appelle la nouvelle méthode du DAO
        // findByCredentials renvoie un Optional (une boite qui peut être vide ou contenir un User)
        var userOptional = userDAO.findByCredentials(email, password);

        // .isPresent() renvoie true si l'utilisateur a été trouvé, false sinon
        return userOptional.isPresent();
    }

    // TODO will jave to delegate to the usermanager
    // todo remplacer nom, prenom par fullname
    public boolean register(String nom, String prenom, String email, String password) {
        // 1. Créer un nouvel objet User
        // Comme User n'a pas de nom/prenom, on utilise l'email comme username pour garantir l'unicité
        User newUser = new User(fullname,password,email);

        return userDAO.createUser(newUser);
    }
}