package uniearn.services.users.security;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class WebcamCaptureService {

    private static final String OUTPUT_DIR = "uploads/intrusion";

    public static File capture(String email) {
        Webcam webcam = null;
        try {
            webcam = Webcam.getDefault(3000); // 3s timeout
            if (webcam == null) {
                System.out.println("⚠ No webcam found — skipping capture");
                return null;
            }

            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();

            // Give the sensor a moment to adjust exposure
            Thread.sleep(300);

            BufferedImage image = webcam.getImage();
            if (image == null) {
                System.out.println("⚠ Webcam returned null image");
                return null;
            }

            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) dir.mkdirs();

            String safeName = email.replaceAll("[^a-zA-Z0-9@._-]", "_");
            File output = new File(dir, System.currentTimeMillis() + "_" + safeName + ".png");
            ImageIO.write(image, "PNG", output);

            System.out.println("✓ Intruder photo captured: " + output.getAbsolutePath());
            return output;

        } catch (Exception e) {
            System.err.println("⚠ Webcam capture failed: " + e.getMessage());
            return null;
        } finally {
            if (webcam != null && webcam.isOpen()) {
                try { webcam.close(); } catch (Exception ignored) {}
            }
        }
    }
}