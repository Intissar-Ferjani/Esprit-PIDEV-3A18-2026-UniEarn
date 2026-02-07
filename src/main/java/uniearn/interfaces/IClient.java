package uniearn.interfaces;

import java.util.List;

public interface IClient<C,U> extends IUser<U>{
    void addClient(C client);
    void updateClient(int id, C client);
    void deleteClient(int id);
    C getClientById(int id);
    List<C> getAllClients();
}
