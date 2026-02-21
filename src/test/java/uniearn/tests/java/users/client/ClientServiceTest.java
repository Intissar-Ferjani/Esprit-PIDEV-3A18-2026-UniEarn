package uniearn.tests.java.users.client;

import org.junit.jupiter.api.*;
import uniearn.model.entities.users.client.Client;
import uniearn.model.enums.UserRole;
import uniearn.services.users.client.ClientService;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientServiceTest {

    private static ClientService clientService;
    private static int testClientUserId;

    @BeforeAll
    static void setup() {
        clientService = new ClientService();
        System.out.println("ClientService initialized");
    }

    @Test
    @Order(1)
    void testAddClient() throws SQLException {
        Client client = new Client(
                "JUnit Client",
                "client@test.com",
                "123456",
                UserRole.CLIENT,
                null,
                500.0,
                4.2,
                "industry",
                "company"
        );

        clientService.addClient(client);

        List<Client> all = clientService.getAllClients();
        Client inserted = all.stream()
                .filter(c -> c.getEmail().equals("client@test.com"))
                .findFirst()
                .orElse(null);

        assertNotNull(inserted);
        testClientUserId = inserted.getIdUser();

        System.out.println("Inserted Client User ID: " + testClientUserId);
    }

    @Test
    @Order(2)
    void testGetClientById() {
        Client client = clientService.getClientById(testClientUserId);

        assertNotNull(client);
        assertEquals("JUnit Client", client.getName());
        assertEquals(500.0, client.getAmount());
    }

    @Test
    @Order(3)
    void testUpdateClient() {
        Client updated = new Client(
                "Updated Client",
                "updated@test.com",
                "999999",
                UserRole.CLIENT,
                null,
                800.0,
                4.8,
                "industry",
                "company"
        );

        clientService.updateClient(testClientUserId, updated);

        Client client = clientService.getClientById(testClientUserId);

        assertNotNull(client);
        assertEquals(800.0, client.getAmount());
        assertEquals(4.8, client.getRating());
    }

    @Test
    @Order(4)
    void testGetAllClients() {
        List<Client> all = clientService.getAllClients();
        assertTrue(all.size() > 0);
    }

    // Optional delete
//    @Test
//    @Order(5)
//    void testDeleteClient() {
//        clientService.deleteClient(testClientUserId);
//        Client client = clientService.getClientById(testClientUserId);
//        assertNull(client);
//    }
}
