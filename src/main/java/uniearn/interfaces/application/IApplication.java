package uniearn.interfaces.application;

import uniearn.model.entities.candidature.application.Application;
import uniearn.model.enums.ApplicationStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for Application DAO
 * Defines contract for application management operations
 */
public interface IApplication {

    // ================== Basic CRUD operations ==================
    void create(Application application) throws SQLException;

    Application read(int idApplication) throws SQLException;

    List<Application> readAll() throws SQLException;

    void update(Application application) throws SQLException;

    void delete(int idApplication) throws SQLException;

    // ================== Specific query methods ==================
    List<Application> getApplicationsByProject(int projectId) throws SQLException;

    List<Application> getApplicationsByFreelancer(int freelancerId) throws SQLException;

    List<Application> getApplicationsByStatus(ApplicationStatus status) throws SQLException;

    // ================== Business logic methods ==================
    void updateStatus(int idApplication, ApplicationStatus newStatus) throws SQLException;

    boolean alreadyApplied(int freelancerId, int projectId) throws SQLException;

    // ================== Advanced features ==================
    List<Application> searchApplications(String searchTerm) throws SQLException;

    int countApplicationsByProject(int projectId) throws SQLException;

    int countApplicationsByStatus(int projectId, ApplicationStatus status) throws SQLException;

    // ================== Controller-specific actions ==================
    void applyToProject(Application application) throws SQLException; // wraps create() with alreadyApplied check

    boolean acceptApplication(int applicationId); // sets ACCEPTED + rejects others

    boolean rejectApplication(int applicationId); // sets REJECTED

    boolean withdrawApplication(int applicationId, int freelancerId); // delete if owned

    ApplicationStatistics getProjectStatistics(int projectId); // total/accepted/rejected/pending

    // ================== Inner class for statistics ==================
    class ApplicationStatistics {
        private final int total;
        private final int accepted;
        private final int rejected;
        private final int pending;

        public ApplicationStatistics(int total, int accepted, int rejected, int pending) {
            this.total = total;
            this.accepted = accepted;
            this.rejected = rejected;
            this.pending = pending;
        }

        public int getTotal() {
            return total;
        }

        public int getAccepted() {
            return accepted;
        }

        public int getRejected() {
            return rejected;
        }

        public int getPending() {
            return pending;
        }

        @Override
        public String toString() {
            return "Total: " + total + ", Accepted: " + accepted + ", Rejected: " + rejected + ", Pending: " + pending;
        }
    }
}
