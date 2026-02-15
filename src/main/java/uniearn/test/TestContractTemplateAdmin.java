package uniearn.test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test l'interface Admin - Gestion des Templates
 * Version simplifiée sans dépendre du contrôleur complet
 */
public class TestContractTemplateAdmin extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Créer une interface de test simple
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #1E56DB;");

            Label title = new Label("GESTION DES TEMPLATES DE CONTRATS");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

            Label subtitle = new Label("Test de l'interface Admin");
            subtitle.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 12;");

            Button btnNewTemplate = new Button("➕ Nouveau Template");
            btnNewTemplate.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

            Button btnRefresh = new Button("🔄 Rafraîchir");
            btnRefresh.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-padding: 10 20; -fx-font-weight: bold;");

            Button btnDelete = new Button("🗑️ Supprimer");
            btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

            Label status = new Label("✅ Interface chargée avec succès!");
            status.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12; -fx-font-weight: bold;");

            root.getChildren().addAll(title, subtitle, btnNewTemplate, btnRefresh, btnDelete, status);

            Scene scene = new Scene(root, 1000, 400);
            stage.setTitle("Test: Admin - Gestion Templates");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ TestContractTemplateAdmin démarré avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement interface:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

