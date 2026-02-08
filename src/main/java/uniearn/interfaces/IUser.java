package uniearn.interfaces;

import java.util.List;

public interface IUser<U>{
    void addUser(U user);
    void updateUser(int id, U user);
    void deleteUser(int id);
    U getUserById(int id);
    List<U> getAllUsers();
}