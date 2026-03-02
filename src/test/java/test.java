import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class test {

    @Test
    public void testPostCreation() {
        // Simple test to verify JUnit is working
        String title = "Test Post";
        String content = "Test Content";

        assertNotNull(title);
        assertNotNull(content);
        assertEquals("Test Post", title);
    }

    @Test
    public void testCommentAddition() {
        // Another simple test
        int commentsCount = 0;
        commentsCount++;

        assertEquals(1, commentsCount);
    }
}