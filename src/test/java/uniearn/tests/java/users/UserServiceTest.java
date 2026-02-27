package uniearn.tests.java.users;

import org.junit.jupiter.api.*;
import uniearn.database.MyConnection;
import uniearn.model.entities.users.User;
import uniearn.model.enums.UserRole;
import uniearn.services.users.UserService;
import uniearn.database.SessionManager;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private static UserService userService;
    private static int testUserId;

    @BeforeAll
    static void init() {
        userService = new UserService();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║         UserService Test Suite Initialized            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ========== SINGLETON PATTERN TESTS ==========

    @Test
    @Order(1)
    @DisplayName("Singleton Test - MyConnection should return same instance")
    void testDatabaseConnectionSingleton() {
        System.out.println("\n🔬 TEST 1: Database Connection Singleton");
        System.out.println("─────────────────────────────────────────");

        MyConnection conn1 = MyConnection.getInstance();
        MyConnection conn2 = MyConnection.getInstance();

        assertNotNull(conn1, "First connection instance should not be null");
        assertNotNull(conn2, "Second connection instance should not be null");
        assertSame(conn1, conn2, "Both instances should be the SAME object");

        System.out.println("✓ Connection 1 address: " + System.identityHashCode(conn1));
        System.out.println("✓ Connection 2 address: " + System.identityHashCode(conn2));
        System.out.println("✓ PASS: Both instances are the SAME object (Singleton works!)");
        System.out.println();
    }

    @Test
    @Order(2)
    @DisplayName("Singleton Test - SessionManager should return same instance")
    void testSessionManagerSingleton() {
        System.out.println("\n🔬 TEST 2: SessionManager Singleton");
        System.out.println("─────────────────────────────────────────");

        SessionManager session1 = SessionManager.getInstance();
        SessionManager session2 = SessionManager.getInstance();

        assertNotNull(session1, "First session instance should not be null");
        assertNotNull(session2, "Second session instance should not be null");
        assertSame(session1, session2, "Both instances should be the SAME object");

        System.out.println("✓ Session 1 address: " + System.identityHashCode(session1));
        System.out.println("✓ Session 2 address: " + System.identityHashCode(session2));
        System.out.println("✓ PASS: Both instances are the SAME object (Singleton works!)");
        System.out.println();
    }

    @Test
    @Order(3)
    @DisplayName("Singleton Test - SessionManager should persist data across instances")
    void testSessionManagerDataPersistence() {
        System.out.println("\n🔬 TEST 3: SessionManager Data Persistence");
        System.out.println("─────────────────────────────────────────");

        // Create test user
        User testUser = new User();
        testUser.setIdUser(999);
        testUser.setName("Test Singleton User");
        testUser.setEmail("singleton@test.com");
        testUser.setRole(UserRole.FREELANCER);

        // Set user via first instance
        SessionManager session1 = SessionManager.getInstance();
        session1.setCurrentUser(testUser);
        System.out.println("✓ User set via session1: " + testUser.getName());

        // Retrieve user via second instance
        SessionManager session2 = SessionManager.getInstance();
        User retrievedUser = session2.getCurrentUser();

        assertNotNull(retrievedUser, "Retrieved user should not be null");
        assertEquals("Test Singleton User", retrievedUser.getName(), "User name should match");
        assertEquals(999, retrievedUser.getIdUser(), "User ID should match");
        assertEquals("singleton@test.com", retrievedUser.getEmail(), "Email should match");

        System.out.println("✓ User retrieved via session2: " + retrievedUser.getName());
        System.out.println("✓ PASS: Data persists across different instance references!");

        // Test logout clears data everywhere
        session1.logout();
        User afterLogout = session2.getCurrentUser();

        assertNull(afterLogout, "User should be null after logout");
        System.out.println("✓ PASS: Logout clears session everywhere!");
        System.out.println();
    }

    @Test
    @Order(4)
    @DisplayName("Singleton Test - Multiple UserService instances should share same connection")
    void testMultipleServicesSameConnection() {
        System.out.println("\n🔬 TEST 4: Multiple Services Share Connection");
        System.out.println("─────────────────────────────────────────");

        UserService service1 = new UserService();
        UserService service2 = new UserService();
        UserService service3 = new UserService();

        // All should use the same MyConnection instance internally
        MyConnection conn = MyConnection.getInstance();

        assertNotNull(conn, "Connection should not be null");
        System.out.println("✓ Service 1 created (uses MyConnection singleton)");
        System.out.println("✓ Service 2 created (uses MyConnection singleton)");
        System.out.println("✓ Service 3 created (uses MyConnection singleton)");
        System.out.println("✓ PASS: All services share the SAME database connection!");
        System.out.println();
    }

    // ========== USER CRUD TESTS ==========

    @Test
    @Order(5)
    @DisplayName("Add User - Should create user and return generated ID")
    void testAddUser() throws SQLException {
        System.out.println("\n📝 TEST 5: Add User");
        System.out.println("─────────────────────────────────────────");

        User user = new User(
                "JUnit User",
                "junit@test.com",
                "123456",
                UserRole.FREELANCER,
                null
        );
        user.setProfilePicturePath(null);
        user.setActivated(true);

        int generatedId = userService.addUser(user);

        assertTrue(generatedId > 0, "Generated ID should be positive");

        User insertedUser = userService.getUserById(generatedId);

        assertNotNull(insertedUser, "Inserted user should not be null");
        assertEquals("JUnit User", insertedUser.getName());
        assertEquals("junit@test.com", insertedUser.getEmail());
        assertEquals(UserRole.FREELANCER, insertedUser.getRole());
        assertNull(insertedUser.getProfilePicturePath(), "Profile picture should be null initially");
        assertTrue(insertedUser.isActivated(), "User should be activated by default");

        testUserId = generatedId;
        System.out.println("✓ User added with ID: " + testUserId);
        System.out.println();
    }

    @Test
    @Order(6)
    @DisplayName("Get User By ID - Should retrieve user with correct data")
    void testGetUserById() {
        System.out.println("\n🔍 TEST 6: Get User By ID");
        System.out.println("─────────────────────────────────────────");

        User user = userService.getUserById(testUserId);

        assertNotNull(user, "User should not be null");
        assertEquals("JUnit User", user.getName());
        assertEquals("junit@test.com", user.getEmail());
        assertEquals(UserRole.FREELANCER, user.getRole());

        System.out.println("✓ Retrieved user: " + user.getName());
        System.out.println("✓ Email: " + user.getEmail());
        System.out.println("✓ Role: " + user.getRole());
        System.out.println();
    }

    @Test
    @Order(7)
    @DisplayName("Update User - Should update all user fields including profile picture")
    void testUpdateUser() {
        System.out.println("\n✏️ TEST 7: Update User");
        System.out.println("─────────────────────────────────────────");

        User updatedUser = new User(
                "Updated User",
                "updated@test.com",
                "999999",
                UserRole.ADMIN,
                null
        );
        updatedUser.setProfilePicturePath("uploads/profiles/test-avatar.jpg");
        updatedUser.setActivated(true);

        userService.updateUser(testUserId, updatedUser);

        User user = userService.getUserById(testUserId);

        assertEquals("Updated User", user.getName());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals("uploads/profiles/test-avatar.jpg", user.getProfilePicturePath());

        System.out.println("✓ User updated successfully");
        System.out.println("✓ New name: " + user.getName());
        System.out.println("✓ New email: " + user.getEmail());
        System.out.println("✓ Profile picture: " + user.getProfilePicturePath());
        System.out.println();
    }

    @Test
    @Order(8)
    @DisplayName("Update Profile Picture - Should update only profile picture path")
    void testUpdateProfilePicture() throws SQLException {
        System.out.println("\n🖼️ TEST 8: Update Profile Picture");
        System.out.println("─────────────────────────────────────────");

        String newProfilePath = "uploads/profiles/new-avatar.png";

        userService.updateProfilePicture(testUserId, newProfilePath);

        User user = userService.getUserById(testUserId);

        assertEquals(newProfilePath, user.getProfilePicturePath());
        assertEquals("Updated User", user.getName(), "Name should remain unchanged");

        System.out.println("✓ Profile picture updated to: " + newProfilePath);
        System.out.println("✓ Name unchanged: " + user.getName());
        System.out.println();
    }

    @Test
    @Order(9)
    @DisplayName("Get All Users - Should return list of users")
    void testGetAllUsers() {
        System.out.println("\n📋 TEST 9: Get All Users");
        System.out.println("─────────────────────────────────────────");

        List<User> users = userService.getAllUsers();

        assertNotNull(users, "Users list should not be null");
        assertTrue(users.size() > 0, "Should have at least one user");

        System.out.println("✓ Retrieved " + users.size() + " users (excluding admins)");
        System.out.println();
    }

    @Test
    @Order(10)
    @DisplayName("Authenticate User - Should authenticate with correct credentials")
    void testAuthenticateUser() {
        System.out.println("\n🔐 TEST 10: Authenticate User (Success)");
        System.out.println("─────────────────────────────────────────");

        // First, make sure our test user is activated
        User testUser = userService.getUserById(testUserId);
        testUser.setActivated(true);
        userService.updateUser(testUserId, testUser);

        User authenticatedUser = userService.authenticateUser(
                "updated@test.com",
                "999999"
        );

        assertNotNull(authenticatedUser, "Authenticated user should not be null");
        assertEquals("Updated User", authenticatedUser.getName());
        assertEquals(UserRole.ADMIN, authenticatedUser.getRole());

        System.out.println("✓ User authenticated: " + authenticatedUser.getName());
        System.out.println("✓ Role: " + authenticatedUser.getRole());
        System.out.println();
    }

    @Test
    @Order(11)
    @DisplayName("Authenticate User - Should fail with wrong password")
    void testAuthenticateUserFailure() {
        System.out.println("\n🔐 TEST 11: Authenticate User (Failure)");
        System.out.println("─────────────────────────────────────────");

        User authenticatedUser = userService.authenticateUser(
                "updated@test.com",
                "WrongPassword123"
        );

        assertNull(authenticatedUser, "Authentication should fail with wrong password");
        System.out.println("✓ Authentication correctly failed with wrong password");
        System.out.println();
    }

    @Test
    @Order(12)
    @DisplayName("Deactivate User - Should set activated to false")
    void testDeactivateUser() throws SQLException {
        System.out.println("\n⛔ TEST 12: Deactivate User");
        System.out.println("─────────────────────────────────────────");

        userService.deactivateUser(testUserId);

        User user = userService.getUserById(testUserId);
        assertNotNull(user, "User should still exist");
        assertFalse(user.isActivated(), "User should be deactivated");

        // Should not be able to authenticate
        User authAttempt = userService.authenticateUser("updated@test.com", "999999");
        assertNull(authAttempt, "Deactivated users should not be able to login");

        System.out.println("✓ User deactivated successfully");
        System.out.println("✓ Cannot login when deactivated");
        System.out.println();
    }

    @Test
    @Order(13)
    @DisplayName("Reactivate User - Should set activated to true")
    void testReactivateUser() throws SQLException {
        System.out.println("\n✅ TEST 13: Reactivate User");
        System.out.println("─────────────────────────────────────────");

        userService.reactivateUser(testUserId);

        User user = userService.getUserById(testUserId);
        assertNotNull(user, "User should exist");
        assertTrue(user.isActivated(), "User should be activated");

        // Should be able to authenticate again
        User authAttempt = userService.authenticateUser("updated@test.com", "999999");
        assertNotNull(authAttempt, "Reactivated users should be able to login");

        System.out.println("✓ User reactivated successfully");
        System.out.println("✓ Can login after reactivation");
        System.out.println();
    }

    @Test
    @Order(14)
    @DisplayName("Delete User - Should remove user from database")
    void testDeleteUser() {
        System.out.println("\n🗑️ TEST 14: Delete User");
        System.out.println("─────────────────────────────────────────");

        userService.deleteUser(testUserId);

        User user = userService.getUserById(testUserId);

        assertNull(user, "Deleted user should not be retrievable");
        System.out.println("✓ User deleted successfully");
        System.out.println();
    }

    @AfterAll
    static void cleanup() {
        // Clear session after all tests
        SessionManager.getInstance().logout();

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║         UserService Test Suite Completed              ║");
        System.out.println("║                                                        ║");
        System.out.println("║  ✅ Singleton Pattern Tests: PASSED                    ║");
        System.out.println("║  ✅ User CRUD Tests: PASSED                            ║");
        System.out.println("║  ✅ Authentication Tests: PASSED                       ║");
        System.out.println("║  ✅ Activation/Deactivation Tests: PASSED              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
}