package dev.studylink.studylink.business;

public class User {
    private int id;
    private String fullname;
    private String passwordHash;
    private String email;



    public User(String fullname, String passwordHash, String email) {
        this.id = 0;
        this.fullname = fullname;
        this.passwordHash = passwordHash;
        this.email = email;
    }
    // pour recuperation depuis la BDD
    public User(int id, String fullname, String email, String passwordHash) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    // GETTERS & SETTERS -----------------------
    // No setter because the id will be given by the UserDAO at the creation of the User
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    // No setter for the password doesn't mean it won't be able to change.
    // We will use a dedicated function like updatePassword(), that will allow to change the password of the user
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "User{id=" + id + ", fullname='" + fullname + "', email='" + email + "'}";
    }
}
