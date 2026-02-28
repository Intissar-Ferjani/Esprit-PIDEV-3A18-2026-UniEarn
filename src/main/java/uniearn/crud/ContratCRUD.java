package uniearn.crud;

import uniearn.model.entities.contracts.Contrat;
import uniearn.services.contracts.ContratService;
import java.util.List;

/**
 * Classe CRUD pour les opérations sur les contrats
 * Simple sans dépendances externes
 */
public class ContratCRUD {

    private ContratService service;

    public ContratCRUD() {
        this.service = new ContratService();
    }

    // ===== CREATE =====
    /**
     * Créer un nouveau contrat
     * @param contrat Contrat à créer
     * @return true si succès, false sinon
     */
    public boolean create(Contrat contrat) {
        if (contrat == null) {
            System.err.println("Erreur: Contrat null");
            return false;
        }
        try {
            return service.createContrat(contrat);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création: " + e.getMessage());
            return false;
        }
    }

    // ===== READ =====
    /**
     * Récupérer tous les contrats
     * @return Liste des contrats
     */
    public List<Contrat> readAll() {
        try {
            return service.getAllContrats();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupérer un contrat par ID
     * @param id ID du contrat
     * @return Contrat trouvé ou null
     */
    public Contrat readById(int id) {
        try {
            return service.getContratById(id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture par ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupérer les contrats d'un client
     * @param clientID ID du client
     * @return Liste des contrats du client
     */
    public List<Contrat> readByClient(int clientID) {
        try {
            return service.getContratsByClient(clientID);
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture par client: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupérer les contrats d'un projet
     * @param projectID ID du projet
     * @return Liste des contrats du projet
     */
    public List<Contrat> readByProject(int projectID) {
        try {
            return service.getContratsByProject(projectID);
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture par projet: " + e.getMessage());
            return List.of();
        }
    }

    // ===== UPDATE =====
    /**
     * Mettre à jour un contrat
     * @param contrat Contrat à mettre à jour
     * @return true si succès, false sinon
     */
    public boolean update(Contrat contrat) {
        if (contrat == null || contrat.getIdContract() == 0) {
            System.err.println("Erreur: Contrat invalide pour la mise à jour");
            return false;
        }
        try {
            return service.updateContrat(contrat);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    /**
     * Signer le contrat par le client
     * @param contractID ID du contrat
     * @return true si succès, false sinon
     */
    public boolean signByClient(int contractID) {
        try {
            return service.signByClient(contractID);
        } catch (Exception e) {
            System.err.println("Erreur lors de la signature client: " + e.getMessage());
            return false;
        }
    }

    /**
     * Signer le contrat par le freelancer
     * @param contractID ID du contrat
     * @return true si succès, false sinon
     */
    public boolean signByFreelancer(int contractID) {
        try {
            return service.signByFreelancer(contractID);
        } catch (Exception e) {
            System.err.println("Erreur lors de la signature freelancer: " + e.getMessage());
            return false;
        }
    }

    // ===== DELETE =====
    /**
     * Supprimer un contrat
     * @param id ID du contrat à supprimer
     * @return true si succès, false sinon
     */
    public boolean delete(int id) {
        if (id <= 0) {
            System.err.println("Erreur: ID invalide");
            return false;
        }
        try {
            return service.deleteContrat(id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    // ===== UTILITAIRES =====
    /**
     * Vérifier si un contrat existe
     * @param id ID du contrat
     * @return true si existe, false sinon
     */
    public boolean exists(int id) {
        return readById(id) != null;
    }

    /**
     * Compter le nombre de contrats
     * @return Nombre de contrats
     */
    public int count() {
        return readAll().size();
    }

    /**
     * Obtenir la statistique des contrats par statut
     * @return Tableau avec [brouillon, signé_client, signé_freelancer, complet]
     */
    public int[] getStatsByStatus() {
        int[] stats = new int[4];
        for (Contrat contrat : readAll()) {
            stats[contrat.getStatus()]++;
        }
        return stats;
    }

    /**
     * Afficher un contrat en format lisible
     * @param contrat Contrat à afficher
     */
    public void displayContrat(Contrat contrat) {
        if (contrat == null) {
            System.out.println("Contrat nul");
            return;
        }
        System.out.println("═══════════════════════════════════════════");
        System.out.println("ID: " + contrat.getIdContract());
        System.out.println("Montant: " + contrat.getAmount() + " DA");
        System.out.println("Début: " + contrat.getStartDate());
        System.out.println("Fin: " + contrat.getEndDate());
        System.out.println("Statut: " + contrat.getStatusString());
        System.out.println("Client ID: " + contrat.getClientID());
        System.out.println("Projet ID: " + contrat.getProjectID());
        System.out.println("Paiement ID: " + contrat.getPaymentID());
        System.out.println("═══════════════════════════════════════════");
    }
}

