package uniearn.interfaces.users.freelancer;

import java.util.List;

public interface IPortfolioItem<P,PI>{
    void addPortfolioItem(P Portfolio,PI portfolioItem);
    void updatePortfolioItem(P Portfolio, int id, PI portfolioItem);
    void deletePortfolioItem(int id);
    PI getPortfolioItemById(int id);
    List<PI> getAllPortfolioItems();
    List<PI> getPortfolioItemsByPortfolioId(int portfolioId);
}
