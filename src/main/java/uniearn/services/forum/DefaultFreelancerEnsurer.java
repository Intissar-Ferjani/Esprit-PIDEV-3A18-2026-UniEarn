package uniearn.services.forum;

import uniearn.database.MyConnection;

import java.sql.*;

/**
 * Ensures a default freelancer (idFreelancer=1) exists so forum comments and reactions work.
 * Runs the required INSERTs automatically if the freelancer is missing.
 */
public class DefaultFreelancerEnsurer {

    private static final int DEFAULT_FREELANCER_ID = 1;

    public static void ensureDefaultFreelancerExists() {
        Connection cn = MyConnection.getInstance().getCnx();
        if (cn == null) return;
        try {
            if (freelancerExists(cn)) return;
            createDefaultFreelancerAndDependencies(cn);
        } catch (SQLException e) {
            System.out.println("DefaultFreelancerEnsurer: " + e.getMessage());
        }
    }

    private static boolean freelancerExists(Connection cn) throws SQLException {
        String sql = "SELECT 1 FROM freelancer WHERE idFreelancer = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, DEFAULT_FREELANCER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void createDefaultFreelancerAndDependencies(Connection cn) throws SQLException {
        try (Statement st = cn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        try {
            insertUser(cn);
            insertClient(cn);
            insertProject(cn);
            insertTask(cn);
            ensureApplicationAndInsert(cn);
            insertPortfolio(cn);
            insertFreelancer(cn);
        } finally {
            try (Statement st = cn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
        System.out.println("Default freelancer (id=1) created for forum.");
    }

    private static void insertUser(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO `user` (idUser, name, email, password, role) VALUES (1, 'Forum User', 'forum@uniearn.local', 'default', 'FREELANCER')";
        cn.createStatement().executeUpdate(sql);
    }

    private static void insertClient(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO client (idClient, amount, rating, userID) VALUES (1, 0, 0, 1)";
        cn.createStatement().executeUpdate(sql);
    }

    private static void insertProject(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO project (idProject, title, description, budget, status, ClientID) VALUES (1, 'Default Project', 'Default', 0, 0, 1)";
        cn.createStatement().executeUpdate(sql);
    }

    private static void insertTask(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO task (idTask, title, description, deadline, TaskStatus, dateAssign, role, priority, idProject) VALUES (1, 0, 0, CURRENT_TIMESTAMP, 'Todo', CURRENT_TIMESTAMP, 'default', 0, 1)";
        cn.createStatement().executeUpdate(sql);
    }

    private static void ensureApplicationAndInsert(Connection cn) {
        try {
            cn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS application (idApplication int(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (idApplication)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            cn.createStatement().executeUpdate("INSERT IGNORE INTO application (idApplication) VALUES (1)");
        } catch (SQLException ignored) { /* table may not exist or already populated */ }
    }

    private static void insertPortfolio(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO portfolio (idPortfolio, title, description, created_At, freelancerId) VALUES (1, 'Default Portfolio', 'Default', CURRENT_TIMESTAMP, 1)";
        cn.createStatement().executeUpdate(sql);
    }

    private static void insertFreelancer(Connection cn) throws SQLException {
        String sql = "INSERT IGNORE INTO freelancer (idFreelancer, pricePerHour, amount, rating, skills, verificationStatus, status, idUser, idApplication, idPortfolio, idTask) VALUES (1, 0, 0, 0, '[]', 'unverified', 'available', 1, 1, 1, 1)";
        cn.createStatement().executeUpdate(sql);
    }
}
