package uniearn.interfaces;

import uniearn.model.entities.Contrat;
import java.util.List;

public interface IContrat {

    /**
     * Créer un nouveau contrat
     */
    boolean createContrat(Contrat contrat);

    /**
     * Récupérer un contrat par ID
     */
    Contrat getContratById(int id);

    /**
     * Récupérer tous les contrats
     */
    List<Contrat> getAllContrats();

    /**
     * Récupérer les contrats par client
     */
    List<Contrat> getContratsByClient(int clientID);

    /**
     * Récupérer les contrats par projet
     */
    List<Contrat> getContratsByProject(int projectID);

    /**
     * Mettre à jour un contrat
     */
    boolean updateContrat(Contrat contrat);

    /**
     * Supprimer un contrat
     */
    boolean deleteContrat(int id);

    /**
     * Signer un contrat par le client
     */
    boolean signByClient(int contractID);

    /**
     * Signer un contrat par le freelancer
     */
    boolean signByFreelancer(int contractID);
}

