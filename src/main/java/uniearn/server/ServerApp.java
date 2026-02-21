package uniearn.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class ServerApp {
    public static void main(String[] args) {
        // Run on a specific port to avoid conflicts, e.g., 8080
        SpringApplication app = new SpringApplication(ServerApp.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8080"));
        app.run(args);
    }
}
