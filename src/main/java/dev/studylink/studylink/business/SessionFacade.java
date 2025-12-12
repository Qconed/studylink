package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserDAO;

public class SessionFacade {

    private final UserDAO userDAO = new MySQLUserDAO();

    public boolean login(String email, String password) {
        // On appelle la nouvelle méthode du DAO
        // findByCredentials renvoie un Optional (une boite qui peut être vide ou contenir un User)
        var userOptional = userDAO.findByCredentials(email, password);

        // .isPresent() renvoie true si l'utilisateur a été trouvé, false sinon
        return userOptional.isPresent();
    }
    public boolean register(String nom, String prenom, String email, String password) {
        // 1. Créer un nouvel objet User
        dev.studylink.studylink.business.User newUser = new dev.studylink.studylink.business.User();

        // 2. Remplir les données
        // Comme User n'a pas de nom/prenom, on utilise l'email comme username pour garantir l'unicité
        newUser.setUsername(email);
        newUser.setPassword(password);
        newUser.setEmail(email);

        // 3. Appeler le DAO pour l'insérer en base
        return userDAO.createUser(newUser);
    }
}