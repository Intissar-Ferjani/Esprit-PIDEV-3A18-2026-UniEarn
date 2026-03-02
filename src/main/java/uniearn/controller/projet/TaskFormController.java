package uniearn.controller.projet;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.projet.Task;
import uniearn.model.enums.taskpriorityenum;
import uniearn.model.enums.taskstatusenum;
import uniearn.services.projet.TaskService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskFormController {

    @FXML
    private Label formTitle;

    @FXML
    private TextField titleField;
    @FXML
    private Label titleError;

    @FXML
    private ComboBox<Project> projectComboBox;
    @FXML
    private Label projectError;

    @FXML
    private TextField roleField;

    @FXML
    private ComboBox<taskpriorityenum> priorityComboBox;

    @FXML
    private ComboBox<taskstatusenum> statusComboBox;

    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Label deadlineError;

    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label descriptionError;

    private Task currentTask;
    private List<Project> clientProjects;
    private final TaskService taskService = new TaskService();
    private Runnable onSaveCallback;

    public void initData(Task task, List<Project> projects, Runnable onSaveCallback) {
        this.clientProjects = projects;
        this.onSaveCallback = onSaveCallback;

        // Initialize ComboBoxes
        priorityComboBox.setItems(FXCollections.observableArrayList(taskpriorityenum.values()));
        statusComboBox.setItems(FXCollections.observableArrayList(taskstatusenum.values()));

        projectComboBox.setItems(FXCollections.observableArrayList(projects));
        projectComboBox.setConverter(new StringConverter<Project>() {
            @Override
            public String toString(Project p) {
                return p != null ? p.getTitle() : "";
            }

            @Override
            public Project fromString(String string) {
                return null; // Not needed
            }
        });

        if (task != null) {
            this.currentTask = task;
            formTitle.setText("Modifier la Tâche");

            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            roleField.setText(task.getRole());

            if (task.getDeadline() != null) {
                deadlinePicker.setValue(task.getDeadline().toLocalDate());
            }

            priorityComboBox.setValue(task.getPriority());
            statusComboBox.setValue(task.getTaskstatus());

            // Find project to select
            for (Project p : projects) {
                if (p.getIdproject() == task.getProjectid()) {
                    projectComboBox.setValue(p);
                    break;
                }
            }
        } else {
            formTitle.setText("Nouvelle Tâche");
            priorityComboBox.getSelectionModel().selectFirst();
            statusComboBox.getSelectionModel().selectFirst();

            if (!projects.isEmpty()) {
                projectComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Title Validation
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            titleError.setText("Le titre est requis.");
            titleError.setVisible(true);
            titleError.setManaged(true);
            isValid = false;
        } else {
            titleError.setVisible(false);
            titleError.setManaged(false);
        }

        // Project Validation
        if (projectComboBox.getValue() == null) {
            projectError.setText("Sélectionnez un projet parent.");
            projectError.setVisible(true);
            projectError.setManaged(true);
            isValid = false;
        } else {
            projectError.setVisible(false);
            projectError.setManaged(false);
        }

        // Deadline Validation
        LocalDate deadline = deadlinePicker.getValue();
        if (deadline == null) {
            deadlineError.setText("La date limite est requise.");
            deadlineError.setVisible(true);
            deadlineError.setManaged(true);
            isValid = false;
        } else if (deadline.isBefore(LocalDate.now())) {
            deadlineError.setText("La date limite ne peut pas être dans le passé.");
            deadlineError.setVisible(true);
            deadlineError.setManaged(true);
            isValid = false;
        } else {
            deadlineError.setVisible(false);
            deadlineError.setManaged(false);
        }

        // Description Validation
        String desc = descriptionArea.getText();
        if (desc == null || desc.trim().isEmpty()) {
            descriptionError.setText("La description est requise.");
            descriptionError.setVisible(true);
            descriptionError.setManaged(true);
            isValid = false;
        } else if (!desc.matches(".*[a-zA-Z].*")) {
            descriptionError.setText("La description doit contenir des lettres.");
            descriptionError.setVisible(true);
            descriptionError.setManaged(true);
            isValid = false;
        } else {
            descriptionError.setVisible(false);
            descriptionError.setManaged(false);
        }

        return isValid;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        // Build Task
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String role = roleField.getText() != null ? roleField.getText().trim() : "";
        taskpriorityenum priority = priorityComboBox.getValue();
        taskstatusenum status = statusComboBox.getValue();
        LocalDateTime deadline = deadlinePicker.getValue().atStartOfDay();
        int projectId = projectComboBox.getValue().getIdproject();

        try {
            if (currentTask == null) {
                // Add NEW Task
                Task newTask = new Task(title, desc, deadline, status, LocalDateTime.now(), role, priority, projectId);
                taskService.addTask(newTask);
            } else {
                // UPDATE Task
                currentTask.setTitle(title);
                currentTask.setDescription(desc);
                currentTask.setRole(role);
                currentTask.setPriority(priority);
                currentTask.setTaskstatus(status);
                currentTask.setDeadline(deadline);
                currentTask.setProjectid(projectId);
                taskService.updateTask(currentTask);
            }

            // Callback and Close
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'enregistrer la tâche");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
