package uniearn.interfaces;

import java.util.List;

public interface IPortfolio<P> {
    void addPortfolio(P portfolio);
    void updatePortfolio(int id, P portfolio);
    void deletePortfolio(int id);
    P getPortfolioById(int id);
    List<P> getAllPortfolios();
}