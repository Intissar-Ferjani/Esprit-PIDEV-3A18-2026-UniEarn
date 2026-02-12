package uniearn.tests.java;

import org.junit.jupiter.api.*;
import uniearn.model.entities.Portfolio;
import uniearn.services.PortfolioService;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioServiceTest {

    private static PortfolioService portfolioService;
    private static int testPortfolioId;

    // ✅ Must match an existing idFreelancer in your DB
    private static final int EXISTING_FREELANCER_ID = 9;

    @BeforeAll
    static void setup() {
        portfolioService = new PortfolioService();
        System.out.println("PortfolioService initialized");
    }

    @Test
    @Order(1)
    void testAddPortfolio() {

        Portfolio portfolio = new Portfolio(
                "JUnit Portfolio",
                "This is a test portfolio",
                new Date(),
                EXISTING_FREELANCER_ID
        );

        portfolioService.addPortfolio(portfolio);

        List<Portfolio> portfolios = portfolioService.getAllPortfolios();

        Portfolio inserted = portfolios.stream()
                .filter(p -> p.getTitle().equals("JUnit Portfolio"))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted);

        testPortfolioId = inserted.getIdPortfolio();

        System.out.println("Inserted Portfolio ID: " + testPortfolioId);
    }

    @Test
    @Order(2)
    void testGetPortfolioById() {

        Portfolio portfolio = portfolioService.getPortfolioById(testPortfolioId);

        assertNotNull(portfolio);
        assertEquals("JUnit Portfolio", portfolio.getTitle());
    }

    @Test
    @Order(3)
    void testUpdatePortfolio() {

        Portfolio updatedPortfolio = new Portfolio(
                "Updated Portfolio",
                "Updated description",
                new Date(),
                EXISTING_FREELANCER_ID
        );

        portfolioService.updatePortfolio(testPortfolioId, updatedPortfolio);

        Portfolio portfolio = portfolioService.getPortfolioById(testPortfolioId);

        assertNotNull(portfolio);
        assertEquals("Updated Portfolio", portfolio.getTitle());
        assertEquals("Updated description", portfolio.getDescription());
    }

    @Test
    @Order(4)
    void testGetAllPortfolios() {

        List<Portfolio> portfolios = portfolioService.getAllPortfolios();

        assertTrue(portfolios.size() > 0);
    }

//    @Test
//    @Order(5)
//    void testDeletePortfolio() {
//
//        portfolioService.deletePortfolio(testPortfolioId);
//
//        Portfolio portfolio = portfolioService.getPortfolioById(testPortfolioId);
//
//        assertNull(portfolio);
//    }

}
