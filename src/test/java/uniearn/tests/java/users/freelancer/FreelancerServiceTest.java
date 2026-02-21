package uniearn.tests.java.users.freelancer;

import org.junit.jupiter.api.*;
import uniearn.model.entities.users.freelancer.Freelancer;
import uniearn.model.enums.Status;
import uniearn.model.enums.UserRole;
import uniearn.model.enums.VerifStatus;
import uniearn.services.users.freelancer.FreelancerService;

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
    void testAddFreelancer() throws Exception {
        // Create freelancer with all Step 2 fields
        Freelancer freelancer = new Freelancer();

        // Step 1 data
        freelancer.setName("JUnit Freelancer");
        freelancer.setEmail("freelancer@test.com");
        freelancer.setPassword("Test123!");
        freelancer.setRole(UserRole.FREELANCER);

        // Step 2 data
        freelancer.setPricePerHour(50.0);
        freelancer.setSkills(new String[]{"Java", "Spring", "JUnit"});
        freelancer.setBio("Experienced software developer with a passion for testing and quality code.");

        // Default values
        freelancer.setAmount(0.0);
        freelancer.setRating(0.0);
        freelancer.setVerificationStatus(VerifStatus.unverified);
        freelancer.setStatus(Status.AVAILABLE);
        freelancer.setIdTask(null);

        // Add to database
        freelancerService.addFreelancer(freelancer);

        // Retrieve and verify
        List<Freelancer> all = freelancerService.getAllFreelancers();
        Freelancer inserted = all.stream()
                .filter(f -> f.getEmail().equals("freelancer@test.com"))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted, "Freelancer should be inserted");
        testFreelancerId = inserted.getIdUser();

        assertEquals("JUnit Freelancer", inserted.getName());
        assertEquals(50.0, inserted.getPricePerHour());
        assertEquals("Experienced software developer with a passion for testing and quality code.", inserted.getBio());
        assertEquals(VerifStatus.unverified, inserted.getVerificationStatus());
        assertNull(inserted.getStudentCardPath(), "Student card should be null initially");

        System.out.println("✓ Test 1 Passed - Freelancer inserted with ID: " + testFreelancerId);
    }

    @Test
    @Order(2)
    void testGetFreelancerById() {
        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);

        assertNotNull(f, "Freelancer should exist");
        assertEquals("JUnit Freelancer", f.getName());
        assertEquals("freelancer@test.com", f.getEmail());
        assertEquals(50.0, f.getPricePerHour());
        assertNotNull(f.getBio(), "Bio should not be null");
        assertTrue(f.getBio().length() >= 50, "Bio should meet minimum length");
        assertNull(f.getIdTask(), "idTask should be null");

        System.out.println("✓ Test 2 Passed - Freelancer retrieved successfully");
    }

    @Test
    @Order(3)
    void testUpdateVerificationData() throws Exception {
        // Simulate Step 3: Upload student card
        String studentCardPath = "/uploads/student_cards/test_card_123.jpg";

        freelancerService.updateVerificationData(
                testFreelancerId,
                studentCardPath,
                VerifStatus.verified
        );

        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);

        assertNotNull(f, "Freelancer should exist");
        assertEquals(studentCardPath, f.getStudentCardPath(), "Student card path should be updated");
        assertEquals(VerifStatus.unverified, f.getVerificationStatus(), "Status should be unverified");

        System.out.println("✓ Test 3 Passed - Verification data updated");
    }

    @Test
    @Order(4)
    void testUpdateFreelancer() {
        Freelancer updated = new Freelancer();

        // Updated data
        updated.setName("Updated JUnit Freelancer");
        updated.setEmail("updated@test.com");
        updated.setPassword("NewPass456!");
        updated.setRole(UserRole.FREELANCER);
        updated.setPricePerHour(75.0);
        updated.setAmount(2000.0);
        updated.setRating(4.8);
        updated.setSkills(new String[]{"Java", "Spring", "Hibernate", "Docker"});
        updated.setBio("Senior developer with extensive experience in enterprise applications and microservices.");
        updated.setStudentCardPath("/uploads/student_cards/verified_card.jpg");
        updated.setVerificationStatus(VerifStatus.verified);
        updated.setStatus(Status.AVAILABLE);
        updated.setIdTask(5);

        freelancerService.updateFreelancer(testFreelancerId, updated);

        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);

        assertNotNull(f, "Freelancer should exist");
        assertEquals("Updated JUnit Freelancer", f.getName());
        assertEquals(75.0, f.getPricePerHour());
        assertEquals(2000.0, f.getAmount());
        assertEquals(4.8, f.getRating());
        assertEquals(5, f.getIdTask());
        assertEquals(VerifStatus.verified, f.getVerificationStatus());
        assertEquals(Status.AVAILABLE, f.getStatus());
        assertTrue(f.getBio().contains("Senior developer"), "Bio should be updated");

        System.out.println("✓ Test 4 Passed - Freelancer updated successfully");
    }

    @Test
    @Order(5)
    void testGetAllFreelancers() {
        List<Freelancer> all = freelancerService.getAllFreelancers();

        assertTrue(all.size() > 0, "Should have at least one freelancer");

        boolean found = all.stream()
                .anyMatch(f -> f.getIdUser() == testFreelancerId);
        assertTrue(found, "Test freelancer should be in the list");

        System.out.println("✓ Test 5 Passed - Retrieved " + all.size() + " freelancers");
    }

    @Test
    @Order(6)
    void testBioValidation() {
        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);

        assertNotNull(f.getBio(), "Bio should not be null");
        assertFalse(f.getBio().trim().isEmpty(), "Bio should not be empty");
        assertTrue(f.getBio().length() >= 50, "Bio should be at least 50 characters");
        assertTrue(f.getBio().length() <= 500, "Bio should not exceed 500 characters");

        System.out.println("✓ Test 6 Passed - Bio validation successful");
    }

    @Test
    @Order(7)
    void testDeleteFreelancer() {
        freelancerService.deleteFreelancer(testFreelancerId);

        Freelancer f = freelancerService.getFreelancerById(testFreelancerId);
        assertNull(f, "Freelancer should be deleted");

        System.out.println("✓ Test 7 Passed - Freelancer deleted successfully");
    }
}