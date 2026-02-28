package uniearn.utils;

import javafx.scene.control.Alert;

/**
 * Classe utilitaire pour afficher des alertes et dialogues
 */
public class DialogUtil {

    /**
     * Affiche une alerte d'information
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Affiche une alerte générique
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

