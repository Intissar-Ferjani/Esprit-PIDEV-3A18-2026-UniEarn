package uniearn.model.entities.candidature.evaluation;

import uniearn.model.enums.EvaluationType;
import java.time.LocalDateTime;
import java.util.Objects;

public class Evaluation {
    private int idEvaluation;
    private int evaluatorId;
    private int evaluatedId;
    private Integer projectId;
    private int rating;
    private String comment;
    private EvaluationType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Evaluation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Evaluation(int evaluatorId, int evaluatedId, Integer projectId, int rating, String comment,
            EvaluationType type) {
        this();
        this.evaluatorId = evaluatorId;
        this.evaluatedId = evaluatedId;
        this.projectId = projectId;
        this.rating = rating;
        this.comment = comment;
        this.type = type;
    }

    public Evaluation(int idEvaluation, int evaluatorId, int evaluatedId, Integer projectId, int rating, String comment,
            EvaluationType type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idEvaluation = idEvaluation;
        this.evaluatorId = evaluatorId;
        this.evaluatedId = evaluatedId;
        this.projectId = projectId;
        this.rating = rating;
        this.comment = comment;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- getters and setters ---
    public int getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(int idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public int getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(int evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public int getEvaluatedId() {
        return evaluatedId;
    }

    public void setEvaluatedId(int evaluatedId) {
        this.evaluatedId = evaluatedId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
        this.updatedAt = LocalDateTime.now();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.updatedAt = LocalDateTime.now();
    }

    public EvaluationType getType() {
        return type;
    }

    public void setType(EvaluationType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isValid() {
        // Project ID is now optional for USER_TO_USER
        boolean basicValid = evaluatorId > 0 && evaluatedId > 0 && evaluatorId != evaluatedId &&
                rating >= 1 && rating <= 5 &&
                comment != null && comment.trim().length() >= 15;

        return basicValid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Evaluation && ((Evaluation) o).idEvaluation == this.idEvaluation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvaluation);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "idEvaluation=" + idEvaluation +
                ", evaluatorId=" + evaluatorId +
                ", evaluatedId=" + evaluatedId +
                ", projectId=" + projectId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
