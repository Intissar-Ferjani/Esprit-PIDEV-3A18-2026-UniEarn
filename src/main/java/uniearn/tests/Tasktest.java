package uniearn.tests;

import javafx.application.Application;
import javafx.stage.Stage;
import uniearn.services.TaskService;
import uniearn.model.entities.Task;
import uniearn.model.enums.taskpriorityenum;
import java.time.LocalDateTime;
import uniearn.model.enums.taskstatusenum;

import java.sql.SQLException;

public class Tasktest  {

    public static  void main(String[] args) {

        try {

            TaskService service = new TaskService();

            //test add task

            Task t = new Task("Créer interface JavaFX","interface organique ,clair et simple", LocalDateTime.now().plusDays(7), taskstatusenum.Done, LocalDateTime.now(),"DEVELOPER", taskpriorityenum.Low,1);
            service.addTask(t);
            System.out.println("Task added successfully!");

            //test delete task
            service.deleteTask(11);
            System.out.println("Task with ID 6 deleted successfully!");

            //test to show all tasks
            System.out.println("All tasks:");
            service.getAllTasks().forEach(task -> {
                System.out.println("ID: " + task.getIdtask() + ", Title: " + task.getTitle() + ", Description: " + task.getDescription() + ", Deadline: " + task.getDeadline() + ", Status: " + task.getTaskstatus() + ", Date Assigned: " + task.getDateAssigned() + ", Role: " + task.getRole() + ", Priority: " + task.getPriority() + ", Project ID: " + task.getProjectid());
            });

            //test update task
            Task t1 = service.getTaskById(4);
            t1.setTitle("Updated Task Title");
            t1.setDescription("Updated Task Description");
            t1.setDeadline(LocalDateTime.now().plusDays(10));
            t1.setTaskstatus(taskstatusenum.InProgress);
            t1.setDateAssigned(LocalDateTime.now());
            t1.setRole("MANAGER");
            t1.setPriority(taskpriorityenum.Low);
            t1.setProjectid(1);
            service.updateTask(4, t1);

            //test to show task by id
            System.out.println("get task by id:");
            Task task = service.getTaskById(4);
            System.out.println("ID: " + task.getIdtask() + ", Title: " + task.getTitle() + ", Description: " + task.getDescription() + ", Deadline: " + task.getDeadline() + ", Status: " + task.getTaskstatus() + ", Date Assigned: " + task.getDateAssigned() + ", Role: " + task.getRole() + ", Priority: " + task.getPriority() + ", Project ID: " + task.getProjectid());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    }

