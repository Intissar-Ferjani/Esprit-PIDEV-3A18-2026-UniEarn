package uniearn.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ContractTemplateAdminApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_admin.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 700);

        primaryStage.setTitle("UniEarn - Gestion des Templates de Contrats (Admin)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

