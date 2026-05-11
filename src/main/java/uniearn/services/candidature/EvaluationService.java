
package uniearn.services.candidature;

import uniearn.interfaces.evaluation.IEvaluation;
import uniearn.model.entities.candidature.evaluation.Evaluation;
import uniearn.model.enums.EvaluationType;
import uniearn.database.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service layer for Evaluation management
 * Implements IEvaluation for DAO operations
 * Provides business logic and helper methods
 */
public class EvaluationService implements IEvaluation {

    private final Connection connection;

    public EvaluationService() {
        this.connection = MyConnection.getInstance().getCnx();
    }

    // ------------------ CRUD ------------------

    @Override
    public void create(Evaluation evaluation) throws SQLException {
        String query = "INSERT INTO evaluation (evaluator_id, evaluated_id, project_id, rating, comment, type, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, evaluation.getEvaluatorId());
            stmt.setInt(2, evaluation.getEvaluatedId());
            if (evaluation.getProjectId() != null) {
                stmt.setInt(3, evaluation.getProjectId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setInt(4, evaluation.getRating());
            stmt.setString(5, evaluation.getComment());
            stmt.setString(6, evaluation.getType() != null ? evaluation.getType().name() : null);
            stmt.setTimestamp(7, Timestamp.valueOf(evaluation.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(evaluation.getUpdatedAt()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next())
                    evaluation.setIdEvaluation(keys.getInt(1));
            }
        }
    }

    @Override
    public Evaluation read(int idEvaluation) throws SQLException {
        String query = "SELECT * FROM evaluation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idEvaluation);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return extractEvaluationFromResultSet(rs);
        }
        return null;
    }

    @Override
    public List<Evaluation> readAll() throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String query = "SELECT * FROM evaluation ORDER BY created_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next())
                list.add(extractEvaluationFromResultSet(rs));
        }
        return list;
    }

    @Override
    public void update(Evaluation evaluation) throws SQLException {
        String query = "UPDATE evaluation SET rating=?, comment=?, type=?, updated_at=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, evaluation.getRating());
            stmt.setString(2, evaluation.getComment());
            stmt.setString(3, evaluation.getType() != null ? evaluation.getType().name() : null);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, evaluation.getIdEvaluation());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int idEvaluation) throws SQLException {
        String query = "DELETE FROM evaluation WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idEvaluation);
            stmt.executeUpdate();
        }
    }

    // ------------------ Queries ------------------

    @Override
    public List<Evaluation> getEvaluationsByEvaluated(int evaluatedId) throws SQLException {
        return getEvaluationsByField("evaluated_id", evaluatedId);
    }

    @Override
    public List<Evaluation> getEvaluationsByEvaluator(int evaluatorId) throws SQLException {
        return getEvaluationsByField("evaluator_id", evaluatorId);
    }

    @Override
    public List<Evaluation> getEvaluationsByProject(int projectId) throws SQLException {
        return getEvaluationsByField("project_id", projectId);
    }

    private List<Evaluation> getEvaluationsByField(String field, int value) throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String query = "SELECT * FROM evaluation WHERE " + field + "=? ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, value);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(extractEvaluationFromResultSet(rs));
        }
        return list;
    }

    // ------------------ Statistics ------------------

    @Override
    public double getAverageRating(int userId) throws SQLException {
        String query = "SELECT AVG(rating) AS avg_rating FROM evaluation WHERE evaluated_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getDouble("avg_rating");
        }
        return 0.0;
    }

    @Override
    public Map<Integer, Integer> getRatingDistribution(int userId) throws SQLException {
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++)
            distribution.put(i, 0);

        String query = "SELECT rating, COUNT(*) AS count FROM evaluation WHERE evaluated_id=? GROUP BY rating";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                distribution.put(rs.getInt("rating"), rs.getInt("count"));
        }
        return distribution;
    }

    @Override
    public int getTotalEvaluations(int userId) throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM evaluation WHERE evaluated_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt("total");
        }
        return 0;
    }

    // ------------------ Business Logic ------------------

    @Override
    public boolean evaluationExists(int evaluatorId, int evaluatedId, int projectId) throws SQLException {
        // If projectId is 0 (which might be passed from old int code), check if we can
        // interpret it as null for comparison
        // But better to just check.
        // If projectId > 0, we check for that specific project.
        // If projectId == 0 or null, we check for generic evaluation?
        // Actually, let's keep it simple: if projectID is provided, check with it.

        String query;
        if (projectId > 0) {
            query = "SELECT COUNT(*) FROM evaluation WHERE evaluator_id=? AND evaluated_id=? AND project_id=?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, evaluatorId);
                stmt.setInt(2, evaluatedId);
                stmt.setInt(3, projectId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } else {
            // RELAXED: Allow multiple generic reviews (no project) for testing purposes
            // or if the user wants to give multiple feedback items to the same person.
            return false;
        }
        return false;
    }

    @Override
    public List<Evaluation> getRecentEvaluations(int userId, int limit) throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String query = "SELECT * FROM evaluation WHERE evaluator_id=? ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(extractEvaluationFromResultSet(rs));
        }
        return list;
    }

    @Override
    public List<Evaluation> getEvaluationsByRating(int userId, int rating) throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String query = "SELECT * FROM evaluation WHERE evaluator_id=? AND rating=? ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, rating);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(extractEvaluationFromResultSet(rs));
        }
        return list;
    }

    // ------------------ Helper Methods for Controller ------------------

    /**
     * Compatibility method for controller: gets evaluations **received** by a user
     */
    public List<Evaluation> getUserEvaluations(int userId) {
        try {
            return getEvaluationsByEvaluated(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void createEvaluation(Evaluation evaluation) throws SQLException {
        if (!evaluation.isValid()) {
            throw new SQLException(
                    "L'évaluation n'est pas valide. Vérifiez le commentaire (min 15 car.) et les informations liées.");
        }

        // SANITIZATION: Treat 0 as null for project foreign key
        if (evaluation.getProjectId() != null && evaluation.getProjectId() == 0) {
            evaluation.setProjectId(null);
        }

        // WORK RELATIONSHIP VALIDATION
        if (evaluation.getType() == EvaluationType.FREELANCER_TO_CLIENT) {
            List<Integer> validClients = getEvaluatableClients(evaluation.getEvaluatorId());
            if (!validClients.contains(evaluation.getEvaluatedId())) {
                throw new SQLException(
                        "Vous ne pouvez évaluer que les clients avec qui vous avez travaillé (candidature acceptée).");
            }
        }

        Integer pId = evaluation.getProjectId();
        if (evaluationExists(evaluation.getEvaluatorId(), evaluation.getEvaluatedId(), pId != null ? pId : 0)) {
            throw new SQLException("Une évaluation existe déjà pour ce projet et ce freelancer.");
        }

        try {
            System.out.println("[DEBUG] Creating Evaluation: Evaluator=" + evaluation.getEvaluatorId() +
                    ", Evaluated=" + evaluation.getEvaluatedId() +
                    ", Project=" + evaluation.getProjectId());

            create(evaluation);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1452) { // MySQL FK failure
                throw new SQLException(
                        "Erreur de lien : Le projet ou l'utilisateur spécifié n'existe pas (ID invalide).");
            }
            throw e;
        }
    }

    public boolean updateEvaluation(int evaluationId, int rating, String comment, int userId) {
        try {
            Evaluation e = read(evaluationId);
            if (e == null || e.getEvaluatorId() != userId)
                return false;
            e.setRating(rating);
            e.setComment(comment);
            if (!e.isValid())
                return false;

            update(e);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvaluation(int evaluationId, int userId) {
        try {
            Evaluation e = read(evaluationId);
            if (e == null || e.getEvaluatorId() != userId)
                return false;
            delete(evaluationId);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean canEvaluate(int evaluatorId, int evaluatedId, int projectId) {
        if (evaluatorId == evaluatedId)
            return false;
        try {
            return !evaluationExists(evaluatorId, evaluatedId, projectId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserRatingStats getUserRatingStats(int userId) {
        try {
            double avg = getAverageRating(userId);
            int total = getTotalEvaluations(userId);
            Map<Integer, Integer> dist = getRatingDistribution(userId);
            return new UserRatingStats(avg, total, dist);
        } catch (SQLException e) {
            e.printStackTrace();
            return new UserRatingStats(0.0, 0, Map.of());
        }
    }

    /**
     * Advanced Logic: Gets a list of unique Client IDs that this freelancer is
     * eligible to evaluate.
     * An eligible client is one who owns a project where the freelancer has an
     * ACCEPTED application.
     */
    public List<Integer> getEvaluatableClients(int freelancerId) throws SQLException {
        List<Integer> clientIds = new ArrayList<>();
        String query = """
                SELECT DISTINCT p.ClientID
                FROM project p
                JOIN application a ON p.idProject = a.project_id
                WHERE a.freelancer_id = ? AND a.status = 'ACCEPTED'
                """;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, freelancerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clientIds.add(rs.getInt("ClientID"));
            }
        }
        return clientIds;
    }

    // ------------------ Helper ------------------

    private Evaluation extractEvaluationFromResultSet(ResultSet rs) throws SQLException {
        int pId = rs.getInt("project_id");
        Integer projectId = rs.wasNull() ? null : pId;

        return new Evaluation(
                rs.getInt("id"),
                rs.getInt("evaluator_id"),
                rs.getInt("evaluated_id"),
                projectId,
                rs.getInt("rating"),
                rs.getString("comment"),
                rs.getString("type") != null ? EvaluationType.fromString(rs.getString("type")) : null,
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }

    // ------------------ Inner class ------------------
    public static class UserRatingStats {
        private final double averageRating;
        private final int totalEvaluations;
        private final Map<Integer, Integer> ratingDistribution;

        public UserRatingStats(double averageRating, int totalEvaluations,
                               Map<Integer, Integer> ratingDistribution) {
            this.averageRating = averageRating;
            this.totalEvaluations = totalEvaluations;
            this.ratingDistribution = ratingDistribution;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalEvaluations() {
            return totalEvaluations;
        }

        public Map<Integer, Integer> getRatingDistribution() {
            return ratingDistribution;
        }

        public String getStarsDisplay() {
            int fullStars = (int) Math.round(averageRating);
            return "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        }

        @Override
        public String toString() {
            return String.format("Rating: %.1f/5.0 %s (%d evaluations)",
                    averageRating, getStarsDisplay(), totalEvaluations);
        }
    }
}