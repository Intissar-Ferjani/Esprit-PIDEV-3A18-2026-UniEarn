package uniearn.services.users.freelancer;

import uniearn.database.MyConnection;
import uniearn.interfaces.users.freelancer.IPortfolio;
import uniearn.model.entities.users.freelancer.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioService implements IPortfolio<Portfolio> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    @Override
    public void addPortfolio(Portfolio portfolio) {

        String sql = "INSERT INTO portfolio (title, description, created_At, freelancer_id) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, portfolio.getTitle());
            ps.setString(2, portfolio.getDescription());
            ps.setTimestamp(3, new Timestamp(portfolio.getCreated_At().getTime()));
            ps.setInt(4, portfolio.getFreelancerId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                portfolio.setIdPortfolio(rs.getInt(1));
            }

            System.out.println("Portfolio added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding portfolio: " + e.getMessage());
            throw new RuntimeException("Failed to add portfolio", e);
        }
    }

    @Override
    public void updatePortfolio(int id, Portfolio portfolio) {

        String sql = "UPDATE portfolio SET title=?, description=? WHERE idPortfolio=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, portfolio.getTitle());
            ps.setString(2, portfolio.getDescription());
            ps.setInt(3, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Portfolio updated successfully!");
            } else {
                System.out.println("No portfolio found with ID: " + id);
                throw new RuntimeException("Portfolio not found");
            }

        } catch (SQLException e) {
            System.out.println("Error updating portfolio: " + e.getMessage());
            throw new RuntimeException("Failed to update portfolio", e);
        }
    }

    @Override
    public void deletePortfolio(int id) {
        // First, delete all portfolio items (if cascade is not set up in database)
        String deleteItemsSql = "DELETE FROM portfolioitem WHERE portfolio_id=?";
        String deletePortfolioSql = "DELETE FROM portfolio WHERE idPortfolio=?";

        try {
            // Start transaction
            cn.setAutoCommit(false);

            // Delete portfolio items first
            PreparedStatement psItems = cn.prepareStatement(deleteItemsSql);
            psItems.setInt(1, id);
            int itemsDeleted = psItems.executeUpdate();
            System.out.println("Deleted " + itemsDeleted + " portfolio items");

            // Then delete portfolio
            PreparedStatement psPortfolio = cn.prepareStatement(deletePortfolioSql);
            psPortfolio.setInt(1, id);
            int portfolioDeleted = psPortfolio.executeUpdate();

            if (portfolioDeleted > 0) {
                cn.commit();
                System.out.println("Portfolio deleted successfully!");
            } else {
                cn.rollback();
                System.out.println("No portfolio found with ID: " + id);
                throw new RuntimeException("Portfolio not found");
            }

            // Reset auto-commit
            cn.setAutoCommit(true);

        } catch (SQLException e) {
            try {
                cn.rollback();
                cn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Error rolling back: " + ex.getMessage());
            }
            System.out.println("Error deleting portfolio: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete portfolio: " + e.getMessage(), e);
        }
    }

    @Override
    public Portfolio getPortfolioById(int id) {

        String sql = "SELECT * FROM portfolio WHERE idPortfolio=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Portfolio p = new Portfolio();

                p.setIdPortfolio(rs.getInt("idPortfolio"));
                p.setTitle(rs.getString("title"));
                p.setDescription(rs.getString("description"));
                p.setCreated_At(rs.getTimestamp("created_At"));
                p.setFreelancerId(rs.getInt("freelancer_id"));

                return p;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve portfolio", e);
        }

        return null;
    }

    @Override
    public List<Portfolio> getAllPortfolios() {

        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM portfolio";

        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Portfolio p = new Portfolio();

                p.setIdPortfolio(rs.getInt("idPortfolio"));
                p.setTitle(rs.getString("title"));
                p.setDescription(rs.getString("description"));
                p.setCreated_At(rs.getTimestamp("created_At"));
                p.setFreelancerId(rs.getInt("freelancer_id"));

                portfolios.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolios: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve portfolios", e);
        }

        return portfolios;
    }
}