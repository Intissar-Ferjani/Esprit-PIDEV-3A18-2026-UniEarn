package uniearn.services.contracts;

import uniearn.model.entities.contracts.Contrat;

import java.util.List;

/**
 * Classe CRUD pour les contrats - Wrapper autour de ContratService
 * Cette classe existe pour la compatibilité avec les anciennes classes de test
 */
public class ContratCRUD {

    private ContratService contratService;

    public ContratCRUD() {
        this.contratService = new ContratService();
    }

    /**
     * Créer un nouveau contrat
     */
    public boolean create(Contrat contrat) {
        return contratService.createContrat(contrat);
    }

    /**
     * Récupérer un contrat par ID
     */
    public Contrat read(int id) {
        return contratService.getContratById(id);
    }

    /**
     * Récupérer tous les contrats
     */
    public List<Contrat> readAll() {
        return contratService.getAllContrats();
    }

    /**
     * Mettre à jour un contrat
     */
    public boolean update(Contrat contrat) {
        return contratService.updateContrat(contrat);
    }

    /**
     * Supprimer un contrat
     */
    public boolean delete(int id) {
        return contratService.deleteContrat(id);
    }

    /**
     * Rechercher des contrats par client
     */
    public List<Contrat> findByClientId(int clientId) {
        return contratService.getContratsByClient(clientId);
    }

    /**
     * Rechercher des contrats par freelancer
     */
    public List<Contrat> findByFreelancerId(int freelancerId) {
        return contratService.getContratsByFreelancer(freelancerId);
    }

    /**
     * Rechercher des contrats par projet
     */
    public List<Contrat> findByProjectId(int projectId) {
        return contratService.getContratsByProject(projectId);
    }

    /**
     * Rechercher des contrats par statut
     */
    public List<Contrat> findByStatus(int status) {
        return contratService.getContratsByStatus(status);
    }

    // Méthodes compatibles avec les anciens noms

    /**
     * Lire un contrat par ID (alias de read)
     */
    public Contrat readById(int id) {
        return read(id);
    }

    /**
     * Rechercher par client (alias)
     */
    public List<Contrat> readByClient(int clientId) {
        return findByClientId(clientId);
    }

    /**
     * Rechercher par projet (alias)
     */
    public List<Contrat> readByProject(int projectId) {
        return findByProjectId(projectId);
    }

    /**
     * Signer un contrat côté client
     */
    public boolean signByClient(int contractId) {
        return contratService.signByClient(contractId);
    }

    /**
     * Signer un contrat côté freelancer
     */
    public boolean signByFreelancer(int contractId) {
        return contratService.signByFreelancer(contractId);
    }

    /**
     * Afficher les détails d'un contrat (pour les tests console)
     */
    public void displayContrat(Contrat contrat) {
        if (contrat == null) {
            System.out.println("❌ Contrat introuvable");
            return;
        }

        System.out.println("\n┌─ DÉTAILS DU CONTRAT ──────────────────┐");
        System.out.println("│ ID: " + contrat.getIdContract());
        System.out.println("│ Type: " + contrat.getType());
        System.out.println("│ Montant: " + contrat.getAmount() + " €");
        System.out.println("│ Date début: " + contrat.getStartDate());
        System.out.println("│ Date fin: " + contrat.getEndDate());
        System.out.println("│ Statut: " + getStatusLabel(contrat.getStatus()));
        System.out.println("│ Client ID: " + contrat.getClientID());
        System.out.println("│ Freelancer ID: " + contrat.getFreelancerID());
        System.out.println("│ Projet ID: " + contrat.getProjectID());
        System.out.println("└───────────────────────────────────────┘\n");
    }

    /**
     * Obtenir les statistiques par statut
     */
    public int[] getStatsByStatus() {
        int[] stats = new int[4]; // Brouillon, En cours, Signé, Annulé
        for (int i = 0; i < 4; i++) {
            List<Contrat> contrats = findByStatus(i);
            stats[i] = contrats != null ? contrats.size() : 0;
        }
        return stats;
    }

    /**
     * Compter le nombre total de contrats
     */
    public int count() {
        List<Contrat> contrats = readAll();
        return contrats != null ? contrats.size() : 0;
    }

    /**
     * Obtenir le label d'un statut
     */
    private String getStatusLabel(int status) {
        switch (status) {
            case 0: return "Brouillon";
            case 1: return "En cours";
            case 2: return "Signé";
            case 3: return "Annulé";
            default: return "Inconnu";
        }
    }
}


