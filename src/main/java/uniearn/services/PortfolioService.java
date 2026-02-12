package uniearn.services;

import uniearn.database.MyConnection;
import uniearn.interfaces.IPortfolio;
import uniearn.model.entities.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioService implements IPortfolio<Portfolio> {

    private final Connection cn = MyConnection.getInstance().getCnx();

    @Override
    public void addPortfolio(Portfolio portfolio) {

        String sql = "INSERT INTO portfolio (title, description, created_At, freelancerId) VALUES (?, ?, ?, ?)";

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
        }
    }

    @Override
    public void updatePortfolio(int id, Portfolio portfolio) {

        String sql = "UPDATE portfolio SET title=?, description=?, created_At=?, freelancerId=? WHERE idPortfolio=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, portfolio.getTitle());
            ps.setString(2, portfolio.getDescription());
            ps.setTimestamp(3, new Timestamp(portfolio.getCreated_At().getTime()));
            ps.setInt(4, portfolio.getFreelancerId());
            ps.setInt(5, id);

            ps.executeUpdate();

            System.out.println("Portfolio updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating portfolio: " + e.getMessage());
        }
    }

    @Override
    public void deletePortfolio(int id) {

        String sql = "DELETE FROM portfolio WHERE idPortfolio=?";

        try {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("Portfolio deleted successfully!");

        } catch (SQLException e) {
            System.out.println("Error deleting portfolio: " + e.getMessage());
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
                p.setFreelancerId(rs.getInt("freelancerId"));

                return p;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio: " + e.getMessage());
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
                p.setFreelancerId(rs.getInt("freelancerId"));

                portfolios.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolios: " + e.getMessage());
        }

        return portfolios;
    }
}
