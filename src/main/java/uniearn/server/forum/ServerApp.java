package uniearn.server.forum;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApp {
    public static void main(String[] args) {
        // Run on a specific port to avoid conflicts, e.g., 8080
        SpringApplication app = new SpringApplication(ServerApp.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
        app.run(args);
    }
}
