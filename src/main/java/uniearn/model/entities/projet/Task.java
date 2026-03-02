package uniearn.model.entities.projet;

import uniearn.model.enums.taskpriorityenum;
import uniearn.model.enums.taskstatusenum;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class Task {
    private int idtask;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private taskstatusenum taskstatus;
    private LocalDateTime dateAssigned;
    private String role;
    private taskpriorityenum  priority;
    private int projectid;

        public Task() {};

        public Task(String title, String description, LocalDateTime deadline, taskstatusenum taskstatus, LocalDateTime dateAssigned, String role, taskpriorityenum priority, int projectid) {
            this.title = title;
            this.description = description;
            this.deadline = deadline;
            this.taskstatus = taskstatus;
            this.dateAssigned = dateAssigned;
            this.role = role;
            this.priority = priority;
            this.projectid = projectid;
        }

        public int getIdtask() {
            return idtask;
        }

        public void setIdtask(int idtask) {
            this.idtask = idtask;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getDeadline() {
            return deadline;
        }

        public void setDeadline(LocalDateTime deadline) {
            this.deadline = deadline;
        }

        public taskstatusenum getTaskstatus() {
            return taskstatus;
        }

        public void setTaskstatus(taskstatusenum taskstatus) {
            this.taskstatus = taskstatus;
        }

        public LocalDateTime getDateAssigned() {
            return dateAssigned;
        }

        public void setDateAssigned(LocalDateTime dateAssigned) {
            this.dateAssigned = dateAssigned;
        }

         public String getRole() {
            return role;
         }

         public void setRole(String role) {
            this.role = role;
         }

          public taskpriorityenum getPriority() {
            return priority;
          }

        public void setPriority(taskpriorityenum priority) {
            this.priority = priority;
        }

        public int getProjectid() {
            return projectid;
        }

        public void setProjectid(int projectid) {
            this.projectid = projectid;
        }

}
