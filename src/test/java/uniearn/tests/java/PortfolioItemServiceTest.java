package uniearn.tests.java;

import org.junit.jupiter.api.*;
import uniearn.model.entities.Portfolio;
import uniearn.model.entities.PortfolioItem;
import uniearn.services.PortfolioItemService;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioItemServiceTest {

    private static PortfolioItemService service;
    private static int testItemId;

    private static final int EXISTING_PORTFOLIO_ID = 2;

    @BeforeAll
    static void setup() {
        service = new PortfolioItemService();
        System.out.println("PortfolioItemService initialized");
    }

    @Test
    @Order(1)
    void testAddPortfolioItem() {

        Portfolio portfolio = new Portfolio();
        portfolio.setIdPortfolio(EXISTING_PORTFOLIO_ID);

        PortfolioItem item = new PortfolioItem(
                "JUnit Item",
                "Test description",
                new String[]{"Java", "Spring"},
                new String[]{"img1.png", "img2.png"},
                "https://project.com",
                "https://github.com/project",
                new Date(),
                EXISTING_PORTFOLIO_ID
        );

        service.addPortfolioItem(portfolio, item);

        List<PortfolioItem> items = service.getAllPortfolioItems();

        PortfolioItem inserted = items.stream()
                .filter(i -> i.getTitle().equals("JUnit Item"))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted);
        testItemId = inserted.getIdItem();
    }

    @Test
    @Order(2)
    void testGetPortfolioItemById() {

        PortfolioItem item = service.getPortfolioItemById(testItemId);

        assertNotNull(item);
        assertEquals("JUnit Item", item.getTitle());
    }

    @Test
    @Order(3)
    void testUpdatePortfolioItem() {

        Portfolio portfolio = new Portfolio();
        portfolio.setIdPortfolio(EXISTING_PORTFOLIO_ID);

        PortfolioItem updated = new PortfolioItem(
                "Updated Item",
                "Updated description",
                new String[]{"React","Node.js"},
                new String[]{"newimg.png"},
                "https://updated.com",
                "https://github.com/updated",
                new Date(),
                EXISTING_PORTFOLIO_ID
        );

        service.updatePortfolioItem(portfolio, testItemId, updated);

        PortfolioItem item = service.getPortfolioItemById(testItemId);

        assertNotNull(item);
        assertEquals("Updated Item", item.getTitle());
    }

    @Test
    @Order(4)
    void testGetPortfolioItemsByPortfolioId() {

        List<PortfolioItem> items = service.getPortfolioItemsByPortfolioId(EXISTING_PORTFOLIO_ID);
        System.out.println(items.stream().map(item -> item.getTitle()).toList());

        assertTrue(items.size() > 0);
    }

//    @Test
//    @Order(5)
//    void testDeletePortfolioItem() {
//
//        service.deletePortfolioItem(testItemId);
//
//        PortfolioItem deleted = service.getPortfolioItemById(testItemId);
//
//        assertNull(deleted);
//    }

}
