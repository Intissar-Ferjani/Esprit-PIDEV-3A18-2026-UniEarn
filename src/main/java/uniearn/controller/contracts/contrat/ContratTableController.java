package uniearn.controller.contracts.contrat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import uniearn.services.contracts.ContratCRUD;
import uniearn.model.entities.contracts.Contrat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Exemple d'intégration du CRUD dans un contrôleur JavaFX
 * Ce fichier montre comment utiliser ContratCRUD dans une interface graphique
 */
public class ContratTableController {

    @FXML
    private TableView<Contrat> tableContrats;
    @FXML
    private TableColumn<Contrat, Integer> colId;
    @FXML
    private TableColumn<Contrat, String> colDateDebut;
    @FXML
    private TableColumn<Contrat, Double> colMontant;
    @FXML
    private TableColumn<Contrat, String> colStatut;

    @FXML
    private TextField tfMontant;
    @FXML
    private DatePicker dpDebut;
    @FXML
    private TextField tfClientID;
    @FXML
    private TextField tfProjetID;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private Button btnSignerClient;
    @FXML
    private Button btnSignerFreelancer;

    private ContratCRUD crud;

    @FXML
    public void initialize() {
        // Initialiser le CRUD
        crud = new ContratCRUD();

        // Configurer les colonnes de la table
        configurerColonnes();

        // Charger les données
        rafraichirTable();

        // Configurer les événements des boutons
        btnAjouter.setOnAction(e -> ajouterContrat());
        btnModifier.setOnAction(e -> modifierContrat());
        btnSupprimer.setOnAction(e -> supprimerContrat());
        btnSignerClient.setOnAction(e -> signerClient());
        btnSignerFreelancer.setOnAction(e -> signerFreelancer());
    }

    // ===== CONFIGURATION =====

    private void configurerColonnes() {
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getIdContract()).asObject());

        colDateDebut.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartDate().toString()));

        colMontant.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getAmount()).asObject());

        colStatut.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatusString()));
    }

    // ===== OPÉRATIONS CRUD =====

    /**
     * Ajouter un nouveau contrat
     */
    private void ajouterContrat() {
        try {
            // Valider les champs
            if (tfMontant.getText().isEmpty() ||
                dpDebut.getValue() == null ||
                tfClientID.getText().isEmpty() ||
                tfProjetID.getText().isEmpty()) {
                afficherAlerte("Erreur", "Veuillez remplir tous les champs");
                return;
            }

            // Créer le contrat
            Contrat contrat = new Contrat();
            contrat.setAmount(Double.parseDouble(tfMontant.getText()));
            contrat.setStartDate(Timestamp.valueOf(dpDebut.getValue().atStartOfDay()));
            contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
            contrat.setStatus(0); // Brouillon
            contrat.setClientID(Integer.parseInt(tfClientID.getText()));
            contrat.setProjectID(Integer.parseInt(tfProjetID.getText()));
            contrat.setPaymentID(0);

            // Sauvegarder
            if (crud.create(contrat)) {
                afficherAlerte("Succès", "Contrat créé avec succès");
                viderChamps();
                rafraichirTable();
            } else {
                afficherAlerte("Erreur", "Impossible de créer le contrat");
            }

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Montant ou ID invalide");
        }
    }

    /**
     * Modifier le contrat sélectionné
     */
    private void modifierContrat() {
        Contrat selected = tableContrats.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un contrat");
            return;
        }

        try {
            if (!tfMontant.getText().isEmpty()) {
                selected.setAmount(Double.parseDouble(tfMontant.getText()));
            }

            if (crud.update(selected)) {
                afficherAlerte("Succès", "Contrat modifié");
                viderChamps();
                rafraichirTable();
            } else {
                afficherAlerte("Erreur", "Impossible de modifier");
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Format invalide");
        }
    }

    /**
     * Supprimer le contrat sélectionné
     */
    private void supprimerContrat() {
        Contrat selected = tableContrats.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un contrat");
            return;
        }

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Êtes-vous sûr?");

        if (confirm.showAndWait().isPresent()) {
            if (crud.delete(selected.getIdContract())) {
                afficherAlerte("Succès", "Contrat supprimé");
                rafraichirTable();
            } else {
                afficherAlerte("Erreur", "Impossible de supprimer");
            }
        }
    }

    /**
     * Signer par le client
     */
    private void signerClient() {
        Contrat selected = tableContrats.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un contrat");
            return;
        }

        if (crud.signByClient(selected.getIdContract())) {
            afficherAlerte("Succès", "Signé par le client");
            rafraichirTable();
        } else {
            afficherAlerte("Erreur", "Impossible de signer");
        }
    }

    /**
     * Signer par le freelancer
     */
    private void signerFreelancer() {
        Contrat selected = tableContrats.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un contrat");
            return;
        }

        if (crud.signByFreelancer(selected.getIdContract())) {
            afficherAlerte("Succès", "Signé par le freelancer");
            rafraichirTable();
        } else {
            afficherAlerte("Erreur", "Impossible de signer");
        }
    }

    // ===== UTILITAIRES =====

    /**
     * Rafraîchir la table avec les dernières données
     */
    private void rafraichirTable() {
        List<Contrat> contrats = crud.readAll();
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        tableContrats.setItems(data);
    }

    /**
     * Vider les champs du formulaire
     */
    private void viderChamps() {
        tfMontant.clear();
        dpDebut.setValue(null);
        tfClientID.clear();
        tfProjetID.clear();
    }

    /**
     * Afficher une alerte
     */
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher les contrats d'un client
     */
    public void afficherContratClient(int clientID) {
        List<Contrat> contrats = crud.readByClient(clientID);
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        tableContrats.setItems(data);
    }

    /**
     * Afficher les contrats d'un projet
     */
    public void afficherContratProjet(int projectID) {
        List<Contrat> contrats = crud.readByProject(projectID);
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        tableContrats.setItems(data);
    }

    /**
     * Récupérer les statistiques
     */
    public int[] obtenirStatistiques() {
        return crud.getStatsByStatus();
    }
}

