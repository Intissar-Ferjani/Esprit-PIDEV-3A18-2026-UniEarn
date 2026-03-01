package uniearn.interfaces.users;


import java.sql.SQLException;
import java.util.List;

public interface IUser<U>{
    int addUser(U user) throws SQLException;
    void updateUser(int id, U user);
    void updateProfilePicture(int id, String path) throws SQLException;
    void deleteUser(int id);
    U getUserById(int id);
    List<U> getAllUsers();
    List<U> getAllActiveUsers();
    U authenticateUser(String email, String password);
    void deactivateUser(int id) throws SQLException;
    void reactivateUser(int id) throws SQLException;
    public boolean emailExists(String email);
    public U getUserByEmail(String email);

}
