package uniearn.interfaces.contracts;

import uniearn.model.entities.contracts.Contrat;
import java.util.List;

public interface IContrat {

    boolean createContrat(Contrat contrat);

    Contrat getContratById(int id);

    List<Contrat> getAllContrats();

    List<Contrat> getContratsByClient(int clientID);

    List<Contrat> getContratsByProject(int projectID);

    boolean updateContrat(Contrat contrat);

    boolean deleteContrat(int id);

    boolean signByClient(int contractID);

    boolean signByFreelancer(int contractID);
}


