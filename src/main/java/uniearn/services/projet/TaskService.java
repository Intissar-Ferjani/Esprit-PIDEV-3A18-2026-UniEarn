package uniearn.services.projet;

import uniearn.database.MyConnection;
import uniearn.model.entities.projet.Task;
import uniearn.model.enums.taskpriorityenum;
import uniearn.model.enums.taskstatusenum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private final Connection cn = MyConnection.getInstance().getCnx();


    public boolean addTask(Task task) throws SQLException {
        String request = "INSERT INTO task (title,description,deadline,TaskStatus,dateAssign,role,priority,idProject) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cn.prepareStatement(request);

        pst.setString(1, task.getTitle());
        pst.setString(2, task.getDescription());
        pst.setObject(3, task.getDeadline());
        pst.setInt(4, task.getTaskstatus().ordinal());
        pst.setObject(5, task.getDateAssigned());
        pst.setString(6, task.getRole());
        pst.setInt(7, task.getPriority().ordinal());
        pst.setInt(8, task.getProjectid());

        pst.executeUpdate();
        return false;
    }

    public boolean updateTask(Task task) {
        String request = "UPDATE task SET title=?, description=?, deadline=?, TaskStatus=?, dateAssign=?, role=?, priority=?, idProject=? WHERE idTask=?";
        try {

            PreparedStatement pst = cn.prepareStatement(request);

            pst.setString(1, task.getTitle());
            pst.setString(2, task.getDescription());
            pst.setObject(3, task.getDeadline());
            pst.setInt(4, task.getTaskstatus().ordinal());
            pst.setObject(5, task.getDateAssigned());
            pst.setString(6, task.getRole());
            pst.setInt(7, task.getPriority().ordinal());
            pst.setInt(8, task.getProjectid());
            pst.setInt(9, task.getIdtask());

            int rows = pst.executeUpdate();

            System.out.println(rows + " row(s) updated.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public boolean deleteTask(int id) {
        String request = "DELETE FROM task WHERE idTask=?";
        try {
            PreparedStatement pst = cn.prepareStatement(request);
            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            System.out.println(rows + " row(s) deleted.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String request = "SELECT * FROM task";
        try {
            PreparedStatement pst = cn.prepareStatement(request);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setIdtask(rs.getInt("idTask"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDeadline(rs.getObject("deadline", LocalDateTime.class));
                task.setTaskstatus(taskstatusenum.valueOf(rs.getString("TaskStatus")));
                task.setDateAssigned(rs.getObject("dateAssign", LocalDateTime.class));
                task.setRole(rs.getString("role"));
                task.setPriority(taskpriorityenum.valueOf(rs.getString("priority")));
                task.setProjectid(rs.getInt("idProject"));
                tasks.add(task);

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
     return tasks;
    }

    public Task getTaskById(int id) {
        String request = "SELECT * FROM task WHERE idTask=?";
        try {
            PreparedStatement pst = cn.prepareStatement(request);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Task task = new Task();
                task.setIdtask(rs.getInt("idTask"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDeadline(rs.getObject("deadline", LocalDateTime.class));
                task.setTaskstatus(taskstatusenum.valueOf(rs.getString("TaskStatus")));
                task.setDateAssigned(rs.getObject("dateAssign", LocalDateTime.class));
                task.setRole(rs.getString("role"));
                task.setPriority(taskpriorityenum.valueOf(rs.getString("priority")));
                task.setProjectid(rs.getInt("idProject"));
                return task;
            } else {
                System.out.println("No task found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}