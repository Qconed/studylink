package dev.studylink.studylink.business;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String email;


    public User(int id, String username, String passwordHash, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    // GETTERS & SETTERS -----------------------
    // No setter because the id will be given by the UserDAO at the creation of the User
    public int getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // No setter for the password doesn't mean it won't be able to change.
    // We will use a dedicated function like updatePassword(), that will allow to change the password of the user
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
