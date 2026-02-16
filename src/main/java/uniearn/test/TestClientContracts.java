package uniearn.test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test l'interface Client - Mes Contrats
 * Version simplifiée sans dépendre du contrôleur complet
 */
public class TestClientContracts extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Créer une interface de test simple
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: white;");

            VBox header = new VBox(10);
            header.setPadding(new Insets(20));
            header.setStyle("-fx-background-color: #1E56DB;");

            Label title = new Label("CRÉER UN CONTRAT");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

            Label subtitle = new Label("Sélectionnez un template et remplissez les informations");
            subtitle.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 12;");

            HBox buttons = new HBox(10);
            Button btnNewContract = new Button("➕ Nouveau Contrat");
            btnNewContract.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

            Button btnRefresh = new Button("🔄 Rafraîchir");
            btnRefresh.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 10 20; -fx-font-weight: bold;");

            buttons.getChildren().addAll(btnNewContract, btnRefresh);
            header.getChildren().addAll(title, subtitle, buttons);

            Label status = new Label(" Interface Client chargée avec succès!");
            status.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12; -fx-font-weight: bold;");

            Label info = new Label("En attente de contrats...");
            info.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

            root.getChildren().addAll(header, status, info);

            Scene scene = new Scene(root, 1000, 500);
            stage.setTitle("Test: Client - Mes Contrats");
            stage.setScene(scene);
            stage.show();

            System.out.println(" TestClientContracts démarré avec succès!");

        } catch (Exception e) {
            System.err.println(" Erreur chargement interface:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

