package uniearn.interfaces.evaluation;

import uniearn.model.entities.candidature.evaluation.Evaluation;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Defines contract for evaluation management operations
 */
public interface IEvaluation {

    // Basic CRUD operations
    void create(Evaluation evaluation) throws SQLException;
    Evaluation read(int idEvaluation) throws SQLException;
    List<Evaluation> readAll() throws SQLException;
    void update(Evaluation evaluation) throws SQLException;
    void delete(int idEvaluation) throws SQLException;

    // Specific query methods
    List<Evaluation> getEvaluationsByEvaluated(int evaluatedId) throws SQLException;
    List<Evaluation> getEvaluationsByEvaluator(int evaluatorId) throws SQLException;
    List<Evaluation> getEvaluationsByProject(int projectId) throws SQLException;

    // Statistics and analytics
    double getAverageRating(int userId) throws SQLException;
    Map<Integer, Integer> getRatingDistribution(int userId) throws SQLException;
    int getTotalEvaluations(int userId) throws SQLException;

    // Business logic methods
    boolean evaluationExists(int evaluatorId, int evaluatedId, int projectId) throws SQLException;
    List<Evaluation> getRecentEvaluations(int userId, int limit) throws SQLException;
    List<Evaluation> getEvaluationsByRating(int userId, int rating) throws SQLException;
}
