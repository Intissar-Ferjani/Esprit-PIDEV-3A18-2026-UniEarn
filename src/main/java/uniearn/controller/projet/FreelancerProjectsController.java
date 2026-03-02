package uniearn.controller.projet;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import uniearn.controller.profile.freelancer.FreelancerDashboardController;
import uniearn.controller.profile.freelancer.FreelancerProfileController;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.services.projet.ProjectService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FreelancerProjectsController {

    @FXML
    private FlowPane projectsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Label totalProjectsLabel;

    private Freelancer currentFreelancer;

    private final ProjectService projectService = new ProjectService();
    private ObservableList<Project> allProjects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Populate status filter options
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous les statuts", "Ouvert", "Terminé"));
        statusFilter.setValue("Tous les statuts");

        // Live search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderProjects());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> renderProjects());
    }

    public void setFreelancerData(Freelancer freelancer) {
        this.currentFreelancer = freelancer;
        loadProjects();
    }

    private void loadProjects() {
        List<Project> projects = projectService.getAllProjects();
        allProjects.setAll(projects);

        // Update stats
        totalProjectsLabel.setText(String.valueOf(projects.size()));

        renderProjects();
    }

    private void renderProjects() {
        projectsContainer.getChildren().clear();

        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String statusVal = statusFilter.getValue();

        List<Project> filtered = allProjects.stream()
                .filter(p -> {
                    boolean matchSearch = p.getTitle().toLowerCase().contains(search)
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(search));
                    boolean matchStatus = true;
                    if (statusVal != null && !statusVal.startsWith("Tous")) {
                        if (statusVal.equals("Ouvert"))
                            matchStatus = (p.getStatus() == 2); // 2 = TODO
                        else if (statusVal.contains("Terminé"))
                            matchStatus = (p.getStatus() == 1); // 1 = Done
                    }
                    return matchSearch && matchStatus;
                })
                .collect(Collectors.toList());

        int delay = 0;
        for (Project project : filtered) {
            Node card = createProjectCard(project, delay++);
            projectsContainer.getChildren().add(card);
        }

        if (filtered.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            empty.setPrefWidth(700);
            Label icon = new Label("🔍");
            icon.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("Aucun projet trouvé");
            msg.setStyle("-fx-font-size: 16px; -fx-text-fill: #657786; -fx-font-weight: bold;");
            empty.getChildren().addAll(icon, msg);
            projectsContainer.getChildren().add(empty);
        }
    }

    private Node createProjectCard(Project project, int delayIndex) {
        VBox card = new VBox(14);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(22));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e1e8ed;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #1976d2;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.15), 14, 0, 0, 4);" +
                        "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e1e8ed;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;"));

        // ── Status pill ──
        String statusText;
        String statusColor;
        String statusBg;
        if (project.getStatus() == 1) { // 1 = Done
            statusText = "✅ Terminé";
            statusColor = "#2e7d32";
            statusBg = "#e8f5e9";
        } else if (project.getStatus() == 2) { // 2 = TODO
            statusText = "🟢 Ouvert";
            statusColor = "#1976d2";
            statusBg = "#e3f2fd";
        } else {
            statusText = "🕒 En cours";
            statusColor = "#ffa000";
            statusBg = "#fff3e0";
        }

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label statusPill = new Label(statusText);
        statusPill.setStyle(
                "-fx-background-color: " + statusBg + ";" +
                        "-fx-text-fill: " + statusColor + ";" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Label idLabel = new Label("#" + project.getIdproject());
        idLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px;");
        headerRow.getChildren().addAll(statusPill, hSpacer, idLabel);

        // ── Title ──
        Label titleLabel = new Label(project.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #14171a;");
        titleLabel.setWrapText(true);

        // ── Description ──
        String desc = project.getDescription() != null ? project.getDescription() : "Aucune description disponible.";
        if (desc.length() > 100)
            desc = desc.substring(0, 97) + "...";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #657786; -fx-font-size: 12.5px;");
        descLabel.setWrapText(true);

        // ── Separator ──
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #f0f3f7;");

        // ── Budget Row ──
        HBox budgetRow = new HBox(8);
        budgetRow.setAlignment(Pos.CENTER_LEFT);
        Label budgetIcon = new Label("💰");
        budgetIcon.setStyle("-fx-font-size: 14px;");
        VBox budgetInfo = new VBox(1);
        Label budgetTitle = new Label("Budget");
        budgetTitle.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label budgetValue = new Label(String.format("%.2f TND", project.getBudget()));
        budgetValue.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 15px; -fx-font-weight: bold;");
        budgetInfo.getChildren().addAll(budgetTitle, budgetValue);
        budgetRow.getChildren().addAll(budgetIcon, budgetInfo);

        // ── Postuler Button ──
        Button postulerBtn = new Button("Postuler →");
        postulerBtn.setMaxWidth(Double.MAX_VALUE);
        postulerBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #1976d2, #4facfe);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 12 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 8, 0, 0, 2);");

        postulerBtn.setOnMouseEntered(e -> {
            postulerBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1565c0, #00c6ff);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.5), 12, 0, 0, 4);");
            postulerBtn.setScaleX(1.02);
            postulerBtn.setScaleY(1.02);
        });

        postulerBtn.setOnMouseExited(e -> {
            postulerBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1976d2, #4facfe);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(25,118,210,0.3), 8, 0, 0, 2);");
            postulerBtn.setScaleX(1.0);
            postulerBtn.setScaleY(1.0);
        });
        postulerBtn.setOnAction(e -> handlePostuler(project));

        boolean alreadyApplied = false;
        try {
            uniearn.services.candidature.ApplicationService applicationService = new uniearn.services.candidature.ApplicationService();
            alreadyApplied = applicationService.alreadyApplied(currentFreelancer.getIdFreelancer(),
                    project.getIdproject());
        } catch (Exception e) {
            // Ignore or log error
        }

        int status = project.getStatus();
        boolean isDone = (status == 1); // 1 = Done in taskstatusenum

        if (isDone || alreadyApplied) {
            postulerBtn.setVisible(false);
            postulerBtn.setManaged(false);

            if (alreadyApplied) {
                Label appliedLabel = new Label("✓ Déjà postulé");
                appliedLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-font-size: 13px;");
                card.getChildren().add(appliedLabel);
            } else if (isDone) {
                Label doneLabel = new Label("☒ Projet terminé");
                doneLabel.setStyle("-fx-text-fill: #657786; -fx-font-weight: bold; -fx-font-size: 13px;");
                card.getChildren().add(doneLabel);
            }
        }

        card.getChildren().addAll(headerRow, titleLabel, descLabel, sep, budgetRow, postulerBtn);

        // Fade-in animation
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(350), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayIndex * 60));
        ft.play();

        return card;
    }

    private void handlePostuler(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/FreelancerDashboardView.fxml"));
            Parent root = loader.load();

            FreelancerDashboardController controller = loader.getController();
            controller.switchToApplicationForm(project.getIdproject());

            Stage stage = (Stage) projectsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Dashboard Freelancer - UniEarn");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible d'ouvrir la page de candidature.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void handlePortfolio() {
        // Simplified: Go back to profile where they can access portfolio properly.
        handleBack();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/freelancer/freelancer-profile.fxml"));
            Parent root = loader.load();
            FreelancerProfileController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);
            Stage stage = (Stage) projectsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Mon Profil - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTaskBoard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile/client/TaskBoard.fxml"));
            Parent root = loader.load();
            TaskBoardController controller = loader.getController();
            controller.setFreelancerData(currentFreelancer);
            Stage stage = (Stage) projectsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Mes Tâches - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        uniearn.database.SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectsContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 750, 600));
            stage.setTitle("Login - UniEarn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
