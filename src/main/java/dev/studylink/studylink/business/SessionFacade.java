package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserDAO;

public class SessionFacade {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(String email, String password) {
        // Logique simple : appel au DAO pour vérifier l'utilisateur
        // return userDAO.validateUser(email, password);

        // Pour tester sans base de données pour l'instant :
        return "admin".equals(email) && "1234".equals(password);
    }
    public boolean register(String nom, String prenom, String email, String password) {
        // TODO: Appeler UserDAO pour sauvegarder en base de données
        // return userDAO.createUser(nom, prenom, email, password);

        // Pour l'instant, on simule que ça marche toujours :
        return true;
    }
}