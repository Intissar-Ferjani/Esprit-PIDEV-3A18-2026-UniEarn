package uniearn.interfaces;

import java.sql.SQLException;
import java.util.List;


public interface ITask<T> {
    void addTask(T task) throws SQLException;

    void updateTask(int id, T task);

    void deleteTask(int id);

    T getTaskById(int id);

    List<T> getAllTasks();

}
