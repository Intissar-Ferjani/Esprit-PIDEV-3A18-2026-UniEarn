package uniearn.tests.java;

import org.junit.jupiter.api.*;
import uniearn.model.entities.Freelancer;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;
import uniearn.services.FreelancerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FreelancerServiceTest {

    private static FreelancerService freelancerService;
    private static int testFreelancerId;

    @BeforeAll
    static void setup() {
        freelancerService = new FreelancerService();
        System.out.println("FreelancerService initialized");
    }

    @Test
    @Order(1)
    void testAddFreelancer() {
        Freelancer freelancer = new Freelancer(
                "JUnit Freelancer",
                "freelancer@test.com",
                "123456",
                UserRole.FREELANCER,
                50.0,
                1000.0,
                4.5,
                new String[]{"Java", "Spring"},
                VerifStatus.unverified,
                Status.AVAILABLE,
                null // idTask nullable
        );

        freelancerService.addFreelancer(freelancer);

        // Get the inserted freelancer from DB
        List<Freelancer> all = freelancerService.getAllFreelancers();
        Freelancer inserted = all.stream()
                .filter(f -> f.getEmail().equals("freelancer@test.com"))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted, "Freelancer should be inserted");
        testFreelancerId = inserted.getIdUser();

        System.out.println("Inserted Freelancer ID: " + testFreelancerId);
    }

    @Test
    @Order(2)
    void testGetFreelancerById() {
        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);
        assertNotNull(f);
        assertEquals("JUnit Freelancer", f.getName());
        assertEquals("freelancer@test.com", f.getEmail());
        assertNull(f.getIdTask(), "idTask should be null");
    }

    @Test
    @Order(3)
    void testUpdateFreelancer() {
        Freelancer updated = new Freelancer(
                "Updated Freelancer",
                "updated@test.com",
                "999999",
                UserRole.FREELANCER,
                75.0,
                2000.0,
                4.8,
                new String[]{"Java", "Spring", "Hibernate"},
                VerifStatus.verified,
                Status.UNAVAILABLE,
                5 // assign idTask
        );

        freelancerService.updateFreelancer(testFreelancerId, updated);

        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);
        assertNotNull(f);
        assertEquals(75.0, f.getPricePerHour());
        assertEquals(2000.0, f.getAmount());
        assertEquals(5, f.getIdTask());
    }

    @Test
    @Order(4)
    void testGetAllFreelancers() {
        List<Freelancer> all = freelancerService.getAllFreelancers();
        assertTrue(all.size() > 0, "Should have at least one freelancer");
    }

//    @Test
//    @Order(5)
//    void testDeleteFreelancer() {
//        freelancerService.deleteFreelancer(testFreelancerId);
//        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);
//        assertNull(f, "Freelancer should be deleted");
//    }
}
