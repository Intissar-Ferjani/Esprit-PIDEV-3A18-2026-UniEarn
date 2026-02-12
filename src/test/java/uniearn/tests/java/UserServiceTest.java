package uniearn.tests.java;

import org.junit.jupiter.api.*;
import uniearn.model.entities.User;
import uniearn.model.enums.UserRole;
import uniearn.services.UserService;

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
    }

    @Test
    @Order(1)
    void testAddUser() throws SQLException {

        User user = new User(
                "JUnit User",
                "junit@test.com",
                "123456",
                UserRole.FREELANCER
        );

        int generatedId = userService.addUser(user);

        assertTrue(generatedId > 0); // make sure ID was generated

        User insertedUser = userService.getUserById(generatedId);

        assertNotNull(insertedUser);
        assertEquals("JUnit User", insertedUser.getName());

        testUserId = generatedId;
    }


    @Test
    @Order(2)
    void testGetUserById() {

        User user = userService.getUserById(testUserId);

        assertNotNull(user);
        assertEquals("JUnit User", user.getName());
        assertEquals(UserRole.FREELANCER, user.getRole());
    }

    @Test
    @Order(3)
    void testUpdateUser() {

        User updatedUser = new User(
                "Updated User",
                "updated@test.com",
                "999999",
                UserRole.ADMIN
        );

        userService.updateUser(testUserId, updatedUser);

        User user = userService.getUserById(testUserId);

        assertEquals("Updated User", user.getName());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    @Order(4)
    void testDeleteUser() {

        userService.deleteUser(testUserId);

        User user = userService.getUserById(testUserId);

        assertNull(user);
    }

    @Test
    @Order(5)
    void testGetAllUsers() {

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
    }
}





//----------- Test by function ------------------
//import org.junit.jupiter.api.Test;
//import uniearn.model.entities.User;
//import uniearn.model.enums.UserRole;
//import uniearn.services.UserService;
//
//import java.sql.SQLException;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class UserServiceTest {
//
//    @Test
//    void testAddUser() throws SQLException {
//
//        UserService userService = new UserService();
//
//        User user = new User(
//                "Manual Test",
//                "manual@test.com",
//                "123456",
//                UserRole.FREELANCER
//        );
//
//        userService.addUser(user);
//
//        assertTrue(true);
//    }
//}
