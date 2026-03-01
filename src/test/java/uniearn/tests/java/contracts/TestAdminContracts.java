package uniearn.tests.java.contracts;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test l'interface Admin - Gestion complète des contrats
 */
public class TestAdminContracts extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // En-tête bleu
            VBox header = new VBox(10);
            header.setPadding(new Insets(20));
            header.setStyle("-fx-background-color: #1E56DB;");

            Label title = new Label("GESTION DES CONTRATS (ADMIN)");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

            Label subtitle = new Label("Gérez tous les contrats, templates et validations");
            subtitle.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 12;");

            HBox buttonBox = new HBox(10);
            buttonBox.setPadding(new Insets(10));

            Button btnNewContract = new Button("➕ Nouveau Contrat");
            btnNewContract.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

            Button btnManageTemplates = new Button("📋 Gérer Templates");
            btnManageTemplates.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

            Button btnRefresh = new Button("🔄 Rafraîchir");
            btnRefresh.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 10 20; -fx-font-weight: bold;");

            buttonBox.getChildren().addAll(btnNewContract, btnManageTemplates, btnRefresh);
            header.getChildren().addAll(title, subtitle, buttonBox);

            // Zone de contenu - Tableau
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            // Filtres
            HBox filterBox = new HBox(15);
            filterBox.setPadding(new Insets(10));
            filterBox.setStyle("-fx-padding: 10; -fx-border-color: #E0E0E0; -fx-border-width: 1;");

            Label filterLabel = new Label("Filtrer par statut:");
            filterLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<String> cbStatus = new ComboBox<>();
            cbStatus.getItems().addAll("Tous", "Brouillon", "En Attente", "Signé Client", "Actif", "Complété");
            cbStatus.setValue("Tous");
            cbStatus.setPrefWidth(150);

            Label searchLabel = new Label("Rechercher:");
            searchLabel.setStyle("-fx-font-weight: bold;");
            TextField tfSearch = new TextField();
            tfSearch.setPromptText("ID ou Type...");
            tfSearch.setPrefWidth(150);

            Button btnSearch = new Button("🔍 Rechercher");

            filterBox.getChildren().addAll(filterLabel, cbStatus, new Separator(), searchLabel, tfSearch, btnSearch);

            // Tableau des contrats
            TableView<ContractRow> tableView = new TableView<>();

            TableColumn<ContractRow, Integer> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colId.setPrefWidth(50);

            TableColumn<ContractRow, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colType.setPrefWidth(100);

            TableColumn<ContractRow, String> colAmount = new TableColumn<>("Montant (DA)");
            colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            colAmount.setPrefWidth(100);

            TableColumn<ContractRow, String> colStatus = new TableColumn<>("Statut");
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colStatus.setPrefWidth(130);

            TableColumn<ContractRow, String> colActions = new TableColumn<>("Actions");
            colActions.setPrefWidth(120);

            tableView.getColumns().addAll(colId, colType, colAmount, colStatus, colActions);

            // Ajouter des données de test
            tableView.getItems().add(new ContractRow(1, "Standard", "75000.0", "Signé Client"));
            tableView.getItems().add(new ContractRow(2, "Premium", "150000.0", "Actif"));

            content.getChildren().addAll(filterBox, tableView);

            // Pied de page
            HBox footer = new HBox(20);
            footer.setPadding(new Insets(15));
            footer.setStyle("-fx-background-color: #F5F5F5;");

            Label lblStats = new Label("Total: 2 contrats");
            lblStats.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

            Label lblSummary = new Label("Actifs: 1 | Signés: 1 | Complétés: 0 | Annulés: 0");
            lblSummary.setStyle("-fx-font-size: 12;");

            footer.getChildren().addAll(lblStats, new Separator(), lblSummary);

            // Layout principal
            VBox root = new VBox();
            root.getChildren().addAll(header, content, footer);
            VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

            Scene scene = new Scene(root, 1400, 800);
            stage.setTitle("Test: Admin - Gestion des Contrats");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ TestAdminContracts démarré avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement interface:");
            e.printStackTrace();
        }
    }

    public static class ContractRow {
        public int id;
        public String type;
        public String amount;
        public String status;

        public ContractRow(int id, String type, String amount, String status) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.status = status;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public String getAmount() { return amount; }
        public String getStatus() { return status; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

