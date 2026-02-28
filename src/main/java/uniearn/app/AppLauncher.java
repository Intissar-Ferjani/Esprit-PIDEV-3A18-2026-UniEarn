package uniearn.app;

/**
 * Launcher alternatif pour démarrer l'application
 * Cette classe est utile pour contourner les restrictions du module JavaFX
 */
public class AppLauncher {

    /**
     * Point d'entrée alternatif - appelle MainApp.main()
     */
    public static void main(String[] args) {
        System.out.println("🚀 Lancement de l'application UniEarn...");
        MainApp.main(args);
    }
}

