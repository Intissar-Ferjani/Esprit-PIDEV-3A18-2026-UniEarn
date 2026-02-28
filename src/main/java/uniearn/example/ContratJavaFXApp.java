package uniearn.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uniearn.controller.contracts.ContratDialogController;
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.contracts.Contrat;

import java.io.IOException;
import java.util.List;

/**
 * Application principale pour la gestion des contrats avec JavaFX
 * Interface graphique moderne avec tableau et ajout de contrats
 */
public class ContratJavaFXApp extends Application {

    private ContratCRUD crud;
    private TableView<Contrat> tableView;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.crud = new ContratCRUD();

        // Créer l'interface principale
        BorderPane root = creerInterface();

        // Créer la scène
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        primaryStage.setTitle("UniEarn - Gestion des Contrats");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Charger les données
        rafraichirTableau();
    }

    /**
     * Créer l'interface principale
     */
    private BorderPane creerInterface() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Haut : Header
        root.setTop(creerHeader());

        // Centre : Tableau
        root.setCenter(creerCentre());

        // Bas : Footer
        root.setBottom(creerFooter());

        return root;
    }

    /**
     * Créer le header avec titre et boutons
     */
    private VBox creerHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: #1E56DB; -fx-padding: 20;");
        header.setSpacing(15);

        // Titre
        Label titre = new Label("GESTION DES CONTRATS");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");

        // Sous-titre
        Label sousTitre = new Label("Gérez vos contrats clients et freelancer");
        sousTitre.setStyle("-fx-text-fill: #EEEEEE; -fx-font-size: 12;");

        // Boutons
        HBox boutonsBox = new HBox();
        boutonsBox.setSpacing(10);
        boutonsBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnAjouter = new Button("➕ Ajouter un Contrat");
        btnAjouter.setStyle("-fx-padding: 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-cursor: hand;");
        btnAjouter.setOnAction(e -> afficherDialogAjout());

        Button btnRafraichir = new Button("🔄 Rafraîchir");
        btnRafraichir.setStyle("-fx-padding: 10 20; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-cursor: hand;");
        btnRafraichir.setOnAction(e -> rafraichirTableau());

        Button btnExporter = new Button("📊 Exporter");
        btnExporter.setStyle("-fx-padding: 10 20; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-cursor: hand;");
        btnExporter.setOnAction(e -> exporterDonnees());

        boutonsBox.getChildren().addAll(btnAjouter, btnRafraichir, btnExporter);

        header.getChildren().addAll(titre, sousTitre, boutonsBox);
        return header;
    }

    /**
     * Créer le centre avec le tableau
     */
    private VBox creerCentre() {
        VBox centre = new VBox();
        centre.setStyle("-fx-padding: 20; -fx-background-color: #F5F5F5;");
        centre.setSpacing(15);

        // Créer le tableau
        tableView = creerTableau();

        // Ajouter au centre
        centre.getChildren().add(tableView);
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);

        return centre;
    }

    /**
     * Créer le tableau des contrats
     */
    private TableView<Contrat> creerTableau() {
        TableView<Contrat> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-border-color: #DDDDDD;");

        // Colonne ID
        TableColumn<Contrat, Integer> colID = new TableColumn<>("ID");
        colID.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        colID.setPrefWidth(50);

        // Colonne Client
        TableColumn<Contrat, Integer> colClient = new TableColumn<>("Client ID");
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientID"));
        colClient.setPrefWidth(80);

        // Colonne Projet
        TableColumn<Contrat, Integer> colProjet = new TableColumn<>("Projet ID");
        colProjet.setCellValueFactory(new PropertyValueFactory<>("projectID"));
        colProjet.setPrefWidth(80);

        // Colonne Montant
        TableColumn<Contrat, Double> colMontant = new TableColumn<>("Montant (DA)");
        colMontant.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMontant.setPrefWidth(120);

        // Colonne Statut
        TableColumn<Contrat, Integer> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStatus()).asObject()
        );
        colStatut.setCellFactory(column -> new TableCell<Contrat, Integer>() {
            @Override
            protected void updateItem(Integer status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    setText(contrat.getStatusString());
                    switch (status) {
                        case 0 -> setStyle("-fx-text-fill: #FF9800;");
                        case 1 -> setStyle("-fx-text-fill: #2196F3;");
                        case 2 -> setStyle("-fx-text-fill: #9C27B0;");
                        case 3 -> setStyle("-fx-text-fill: #4CAF50;");
                        default -> setStyle("");
                    }
                }
            }
        });
        colStatut.setPrefWidth(120);

        // Colonne Actions
        TableColumn<Contrat, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(column -> new TableCell<Contrat, Void>() {
            private final Button btnEditer = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnEditer.setStyle("-fx-padding: 5 10; -fx-background-color: #1E56DB; -fx-text-fill: white; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-padding: 5 10; -fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-cursor: hand;");

                btnEditer.setOnAction(e -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    editerContrat(contrat);
                });

                btnSupprimer.setOnAction(e -> {
                    Contrat contrat = getTableView().getItems().get(getIndex());
                    supprimerContrat(contrat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, btnEditer, btnSupprimer);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(colID, colClient, colProjet, colMontant, colStatut, colActions);
        return table;
    }

    /**
     * Créer le footer
     */
    private HBox creerFooter() {
        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #DDDDDD; -fx-border-width: 1 0 0 0;");
        footer.setSpacing(20);

        statusLabel = new Label("En attente...");
        statusLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");

        Label statsLabel = new Label();
        statsLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

        // Mettre à jour les stats
        int total = crud.count();
        int[] stats = crud.getStatsByStatus();
        statsLabel.setText(String.format("Total: %d | Brouillon: %d | Signé Client: %d | Complété: %d",
                total, stats[0], stats[1], stats[3]));

        footer.getChildren().addAll(statusLabel, new Separator(), statsLabel);
        return footer;
    }

    /**
     * Afficher le dialog d'ajout de contrat
     */
    private void afficherDialogAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contrat_add_dialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Ajouter un Contrat");
            dialog.setScene(new Scene(root, 600, 550));
            dialog.setResizable(false);

            // Centrer sur l'écran principal
            dialog.initOwner(tableView.getScene().getWindow());

            dialog.showAndWait();

            // Rafraîchir le tableau après ajout
            rafraichirTableau();
            statusLabel.setText("✓ Contrat ajouté");

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    /**
     * Éditer un contrat
     */
    private void editerContrat(Contrat contrat) {
        afficherMessage("Édition de contrat #" + contrat.getIdContract() + " en cours...");
        // À implémenter
    }

    /**
     * Supprimer un contrat
     */
    private void supprimerContrat(Contrat contrat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Supprimer le contrat #" + contrat.getIdContract() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (crud.delete(contrat.getIdContract())) {
                afficherMessage("✓ Contrat supprimé");
                rafraichirTableau();
            } else {
                afficherErreur("Erreur", "Impossible de supprimer le contrat");
            }
        }
    }

    /**
     * Rafraîchir le tableau
     */
    private void rafraichirTableau() {
        List<Contrat> contrats = crud.readAll();
        ObservableList<Contrat> data = FXCollections.observableArrayList(contrats);
        tableView.setItems(data);
        statusLabel.setText("✓ Tableau rafraîchi (" + contrats.size() + " contrats)");
    }

    /**
     * Exporter les données
     */
    private void exporterDonnees() {
        afficherMessage("Export en cours... (fonctionnalité à implémenter)");
    }

    /**
     * Afficher un message
     */
    private void afficherMessage(String message) {
        statusLabel.setText(message);
    }

    /**
     * Afficher une erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

