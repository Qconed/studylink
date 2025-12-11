package dev.studylink.studylink.business;
// Session facade will allow to the UI to easiliy use the business logic (for now there is only the user management)
// follows the singleton desing pattern
public class SessionFacade {
    private static SessionFacade instance = null;
    private UserManager userManager = UserManager.getUserManager(); // delegate for the user management

    // CONSTRUCTION -------------------
    private SessionFacade() {}

    // methode d'obtention d'instance singleton
    public static SessionFacade getSessionFacade() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }

    // METHODS -----------------------
    public Boolean register(String password, String username){
        return userManager.register(password, username);
    }

    public User login(String password, String username) throws Exception {
        return userManager.login(password, username);
    }
}