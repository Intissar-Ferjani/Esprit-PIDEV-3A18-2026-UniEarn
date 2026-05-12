package uniearn.interfaces.users.client;

import uniearn.interfaces.users.IUser;

import java.sql.SQLException;
import java.util.List;

public interface IClient<C,U> extends IUser<U> {
    void addClient(C client) throws SQLException;
    void updateClient(int id, C client);
    void deleteClient(int id);
    C getClientById(int id);
    List<C> getAllClients();
}
