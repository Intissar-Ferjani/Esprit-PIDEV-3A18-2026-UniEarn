package uniearn.model.entities;

import uniearn.model.enums.UserRole;

public class User {
    private int idUser;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private String profilePicturePath;
    private boolean activated;

    public User() {
        this.activated = true;
    }

    public User(String name, String email, String password, UserRole role, String profilePicturePath) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePicturePath = profilePicturePath;
        this.activated = true;
    }

    public User(int idUser, String name, String email, String password, UserRole role, String profilePicturePath, boolean activated) {
        this.idUser = idUser;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePicturePath = profilePicturePath;
        this.activated = activated;
    }

    public User(String name, String email, String password, UserRole role) {
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }


    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", profilePicturePath='" + profilePicturePath + '\'' +
                ", activated=" + activated +
                '}';
    }
}
