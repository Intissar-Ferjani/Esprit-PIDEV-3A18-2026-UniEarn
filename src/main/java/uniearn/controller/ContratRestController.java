package uniearn.controller;

import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;

import java.util.List;

/**
 * Classe de contrôle pour les opérations REST sur les contrats
 * (Peut être utilisée pour une API ou un Web Service)
 */
public class ContratRestController {

    private ContratService contratService;

    public ContratRestController() {
        this.contratService = new ContratService();
    }

    /**
     * GET - Récupérer tous les contrats
     */
    public List<Contrat> getAllContrats() {
        return contratService.getAllContrats();
    }

    /**
     * GET - Récupérer un contrat par ID
     */
    public Contrat getContratById(int id) {
        return contratService.getContratById(id);
    }

    /**
     * GET - Récupérer les contrats par client
     */
    public List<Contrat> getContratsByClient(int clientID) {
        return contratService.getContratsByClient(clientID);
    }

    /**
     * GET - Récupérer les contrats par projet
     */
    public List<Contrat> getContratsByProject(int projectID) {
        return contratService.getContratsByProject(projectID);
    }

    /**
     * GET - Récupérer les contrats par statut
     */
    public List<Contrat> getContratsByStatus(int status) {
        return contratService.getContratsByStatus(status);
    }

    /**
     * POST - Créer un contrat
     */
    public boolean createContrat(Contrat contrat) {
        return contratService.createContrat(contrat);
    }

    /**
     * PUT - Mettre à jour un contrat
     */
    public boolean updateContrat(Contrat contrat) {
        return contratService.updateContrat(contrat);
    }

    /**
     * DELETE - Supprimer un contrat
     */
    public boolean deleteContrat(int id) {
        return contratService.deleteContrat(id);
    }

    /**
     * POST - Signer un contrat par le client
     */
    public boolean signByClient(int contractID) {
        return contratService.signByClient(contractID);
    }

    /**
     * POST - Signer un contrat par le freelancer
     */
    public boolean signByFreelancer(int contractID) {
        return contratService.signByFreelancer(contractID);
    }
}

