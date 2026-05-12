package uniearn.database;

import uniearn.model.entities.users.User;

/**
 * Singleton class for managing user session
 * Keeps track of currently logged-in user across the entire application
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ Session started for: " + user.getName() + " (" + user.getRole() + ")");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("✓ Session ended for: " + currentUser.getName());
            currentUser = null;
        }
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getIdUser() : -1;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : "Guest";
    }
}