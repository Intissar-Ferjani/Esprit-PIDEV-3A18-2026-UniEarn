package uniearn.model.entities.candidature.application;

import uniearn.model.enums.ApplicationStatus;
import java.time.LocalDateTime;
import java.util.Objects;

public class Application {
    private int idApplication;
    private int freelancerId;
    private int projectId;
    private ApplicationStatus status;
    private String coverLetter;
    private double proposedBudget; // NEW
    private int estimatedDuration; // NEW
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public Application() {
        this.status = ApplicationStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // NEW constructor used in controller
    public Application(int freelancerId, int projectId, String coverLetter,
            double proposedBudget, int estimatedDuration) {
        this();
        this.freelancerId = freelancerId;
        this.projectId = projectId;
        this.coverLetter = coverLetter;
        this.proposedBudget = proposedBudget;
        this.estimatedDuration = estimatedDuration;
    }

    public Application(int idApplication, int freelancerId, int projectId,
            ApplicationStatus status, String coverLetter,
            double proposedBudget, int estimatedDuration,
            LocalDateTime appliedAt, LocalDateTime updatedAt) {
        this.idApplication = idApplication;
        this.freelancerId = freelancerId;
        this.projectId = projectId;
        this.status = status;
        this.coverLetter = coverLetter;
        this.proposedBudget = proposedBudget;
        this.estimatedDuration = estimatedDuration;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public double getProposedBudget() {
        return proposedBudget;
    } // NEW

    public void setProposedBudget(double proposedBudget) {
        this.proposedBudget = proposedBudget;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    } // NEW

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isValid() {
        return coverLetter != null && !coverLetter.trim().isEmpty() && coverLetter.length() >= 20
                && proposedBudget > 0 && estimatedDuration > 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Application && ((Application) o).idApplication == this.idApplication;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idApplication);
    }

    @Override
    public String toString() {
        return "Application{" +
                "idApplication=" + idApplication +
                ", freelancerId=" + freelancerId +
                ", projectId=" + projectId +
                ", status=" + status +
                ", coverLetter='" + coverLetter + '\'' +
                ", proposedBudget=" + proposedBudget +
                ", estimatedDuration=" + estimatedDuration +
                ", appliedAt=" + appliedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
