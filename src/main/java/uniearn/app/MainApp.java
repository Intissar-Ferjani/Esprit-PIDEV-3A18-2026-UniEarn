package uniearn.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uniearn.server.security.SecurityCallbackServer;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/home/HomePage.fxml"));

        Scene scene = new Scene(root, 1100, 700);

        primaryStage.setTitle("UniEarn — Plateforme de freelancing étudiant");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    @Override
    public void stop() throws Exception {
        SecurityCallbackServer.stop();
        super.stop();
    }



    public static void main(String[] args) {
        launch(args);
    }
}