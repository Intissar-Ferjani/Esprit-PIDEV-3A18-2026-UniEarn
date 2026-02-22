package uniearn.model.entities.users.admin;

import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;

public class Admin extends User {
    private int idAdmin;

    public Admin() {
    }

    public Admin(String name, String email, String password, UserRole role, String profilePicturePath, int idAdmin) {
        super(name, email, password, role, profilePicturePath);
        this.idAdmin = idAdmin;
    }

    public Admin(int idUser, String name, String email, String password, UserRole role, String profilePicturePath, boolean activated, int idAdmin) {
        super(idUser, name, email, password, role, profilePicturePath, activated);
        this.idAdmin = idAdmin;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "idAdmin=" + idAdmin +
                '}';
    }
}
