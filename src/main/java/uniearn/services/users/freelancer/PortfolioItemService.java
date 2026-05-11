package uniearn.services.users.freelancer;

import uniearn.database.MyConnection;
import uniearn.interfaces.users.freelancer.IPortfolioItem;
import uniearn.model.entities.users.freelancer.Portfolio;
import uniearn.model.entities.users.freelancer.PortfolioItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioItemService implements IPortfolioItem<Portfolio, PortfolioItem> {

    private Connection cnx;

    public PortfolioItemService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ===== JSON HELPERS =====

    private String arrayToJson(String[] array) {
        if (array == null || array.length == 0) return "[]";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append("\"").append(array[i]).append("\"");
            if (i < array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String[] jsonToArray(String json) {
        if (json == null || json.length() < 2) return new String[0];

        json = json.substring(1, json.length() - 1); // remove [ ]
        if (json.trim().isEmpty()) return new String[0];

        String[] parts = json.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace("\"", "").trim();
        }
        return parts;
    }


    @Override
    public void addPortfolioItem(Portfolio portfolio, PortfolioItem item) {

        String sql = "INSERT INTO portfolioitem " +
                "(title, description, technologies, imageUrl, projectUrl, githubUrl, portfolio_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setString(3, arrayToJson(item.getTechnologies()));
            ps.setString(4, arrayToJson(item.getImagesUrl())); // assuming imageUrl is JSON
            ps.setString(5, item.getProjectUrl());
            ps.setString(6, item.getGithubUrl());
            ps.setInt(7, portfolio.getIdPortfolio());

            ps.executeUpdate();
            System.out.println("PortfolioItem added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding portfolio item: " + e.getMessage());
        }
    }


    @Override
    public void updatePortfolioItem(Portfolio portfolio, int id, PortfolioItem item) {

        String sql = "UPDATE portfolioitem SET " +
                "title=?, description=?, technologies=?, imageUrl=?, projectUrl=?, githubUrl=? " +
                "WHERE idItem=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setString(3, arrayToJson(item.getTechnologies()));
            ps.setString(4, arrayToJson(item.getImagesUrl()));
            ps.setString(5, item.getProjectUrl());
            ps.setString(6, item.getGithubUrl());
            ps.setInt(7, id);

            ps.executeUpdate();
            System.out.println("PortfolioItem updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating portfolio item: " + e.getMessage());
        }
    }


    @Override
    public void deletePortfolioItem(int id) {

        String sql = "DELETE FROM portfolioitem WHERE idItem=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting portfolio item: " + e.getMessage());
        }
    }


    @Override
    public PortfolioItem getPortfolioItemById(int id) {

        String sql = "SELECT * FROM portfolioitem WHERE idItem=?";
        PortfolioItem item = null;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                item = new PortfolioItem();
                item.setIdItem(rs.getInt("idItem"));
                item.setTitle(rs.getString("title"));
                item.setDescription(rs.getString("description"));
                item.setTechnologies(jsonToArray(rs.getString("technologies")));
                item.setImagesUrl(jsonToArray(rs.getString("imageUrl")));
                item.setProjectUrl(rs.getString("projectUrl"));
                item.setGithubUrl(rs.getString("githubUrl"));
                item.setCreated_At(rs.getTimestamp("created_At"));
                item.setIdPortfolio(rs.getInt("portfolio_id"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio item: " + e.getMessage());
        }

        return item;
    }


    @Override
    public List<PortfolioItem> getAllPortfolioItems() {

        List<PortfolioItem> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolioitem";

        try (Statement st = cnx.createStatement()) {
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                PortfolioItem item = new PortfolioItem();
                item.setIdItem(rs.getInt("idItem"));
                item.setTitle(rs.getString("title"));
                item.setDescription(rs.getString("description"));
                item.setTechnologies(jsonToArray(rs.getString("technologies")));
                item.setImagesUrl(jsonToArray(rs.getString("imageUrl")));
                item.setProjectUrl(rs.getString("projectUrl"));
                item.setGithubUrl(rs.getString("githubUrl"));
                item.setCreated_At(rs.getTimestamp("created_At"));
                item.setIdPortfolio(rs.getInt("portfolio_id"));

                list.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio items: " + e.getMessage());
        }

        return list;
    }


    @Override
    public List<PortfolioItem> getPortfolioItemsByPortfolioId(int portfolioId) {

        List<PortfolioItem> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolioitem WHERE portfolio_id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, portfolioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PortfolioItem item = new PortfolioItem();
                item.setIdItem(rs.getInt("idItem"));
                item.setTitle(rs.getString("title"));
                item.setDescription(rs.getString("description"));
                item.setTechnologies(jsonToArray(rs.getString("technologies")));
                item.setImagesUrl(jsonToArray(rs.getString("imageUrl")));
                item.setProjectUrl(rs.getString("projectUrl"));
                item.setGithubUrl(rs.getString("githubUrl"));
                item.setCreated_At(rs.getTimestamp("created_At"));
                item.setIdPortfolio(rs.getInt("portfolio_id"));

                list.add(item);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio items: " + e.getMessage());
        }

        return list;
    }
}
