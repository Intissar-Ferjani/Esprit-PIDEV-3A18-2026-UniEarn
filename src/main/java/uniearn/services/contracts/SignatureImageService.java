package uniearn.services.contracts;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * Service pour capturer et gérer les signatures électroniques
 * Convertit les dessins du Canvas en images PNG et les stocke en Base64 ou binaire
 */
public class SignatureImageService {

    /**
     * Capturer l'image du Canvas de signature
     * @param canvas Le Canvas contenant la signature
     * @return L'image capturée en tant que Image JavaFX
     */
    public static Image captureCanvasImage(Canvas canvas) {
        try {
            WritableImage snapshot = new WritableImage(
                (int) canvas.getWidth(),
                (int) canvas.getHeight()
            );
            canvas.snapshot(null, snapshot);
            return snapshot;
        } catch (Exception e) {
            System.err.println("Erreur lors de la capture de l'image du Canvas: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convertir une image JavaFX en tableau de bytes (Format PNG réel)
     * Compatible avec Symfony et les navigateurs Web
     * @param image L'image JavaFX à convertir
     * @return Le tableau de bytes au format PNG
     */
    public static byte[] imageToPngBytes(Image image) {
        if (image == null) {
            return null;
        }

        try {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // Créer une BufferedImage (AWT) pour ImageIO
            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
            );

            // Copier les pixels de JavaFX Image vers BufferedImage
            PixelReader reader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bufferedImage.setRGB(x, y, reader.getArgb(x, y));
                }
            }

            // Écrire en PNG dans un ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", baos);
            
            byte[] pngBytes = baos.toByteArray();
            System.out.println("DEBUG: Image convertie en PNG réel - Taille: " + pngBytes.length + " bytes");
            return pngBytes;

        } catch (IOException e) {
            System.err.println("Erreur lors de la conversion de l'image en PNG: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convertir une image JavaFX en Base64
     * @param image L'image JavaFX à convertir
     * @return La chaîne Base64 de l'image PNG
     */
    public static String imageToBase64(Image image) {
        byte[] imageBytes = imageToPngBytes(image);
        if (imageBytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Convertir le Canvas de signature directement en bytes
     * @param canvas Le Canvas contenant la signature
     * @return Le tableau de bytes contenant les pixels
     */
    public static byte[] canvasToPngBytes(Canvas canvas) {
        if (canvas == null) {
            System.err.println("ERROR: Canvas est null!");
            return null;
        }

        System.out.println("DEBUG canvasToPngBytes:");
        System.out.println("  - Dimensions: " + canvas.getWidth() + "x" + canvas.getHeight());

        Image image = captureCanvasImage(canvas);
        if (image == null) {
            System.err.println("ERROR: Impossible de capturer l'image du Canvas!");
            return null;
        }

        System.out.println("  - Image capturée: " + image.getWidth() + "x" + image.getHeight());

        byte[] bytes = imageToPngBytes(image);

        if (bytes == null || bytes.length == 0) {
            System.err.println("ERROR: Image vide ou null!");
            return null;
        }

        System.out.println("  - Bytes générés: " + bytes.length + " bytes");

        // Vérifier si l'image a du contenu
        boolean hasContent = canvasHasSignature(canvas);
        System.out.println("  - Contient une signature: " + hasContent);

        return bytes;
    }

    /**
     * Convertir le Canvas de signature directement en Base64
     * @param canvas Le Canvas contenant la signature
     * @return La chaîne Base64 de l'image PNG
     */
    public static String canvasToBase64(Canvas canvas) {
        byte[] imageBytes = canvasToPngBytes(canvas);
        if (imageBytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Convertir des bytes en Image JavaFX
     * @param imageBytes Le tableau de bytes
     * @return L'image JavaFX
     */
    public static Image bytesToImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            System.err.println("ERROR: imageBytes insuffisant");
            return null;
        }

        // --- Tentative 1: Format standard (PNG, JPG, etc.) ---
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
            Image standardImage = new Image(bais);
            if (!standardImage.isError() && standardImage.getWidth() > 0) {
                System.out.println("DEBUG: Image chargée au format standard (PNG/JPG)");
                return standardImage;
            }
        } catch (Exception e) {
            // Ignorer et essayer le format custom
        }

        // --- Tentative 2: Format custom ARGB (UniEarn Java) ---
        try {
            // Lire les dimensions (4 premiers bytes)
            int width = ((imageBytes[0] & 0xFF) << 8) | (imageBytes[1] & 0xFF);
            int height = ((imageBytes[2] & 0xFF) << 8) | (imageBytes[3] & 0xFF);

            System.out.println("DEBUG bytesToImage (Custom): width=" + width + ", height=" + height);

            if (width <= 0 || height <= 0 || width > 5000 || height > 5000) {
                System.err.println("ERROR: Dimensions invalides ou trop grandes pour format custom");
                return null;
            }

            // Créer une WritableImage
            WritableImage image = new WritableImage(width, height);

            // Lire les pixels (4 bytes par pixel: ARGB)
            int pixelIndex = 4; // Après les 4 bytes de dimensions
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (pixelIndex + 4 <= imageBytes.length) {
                        int alpha = imageBytes[pixelIndex++] & 0xFF;
                        int red = imageBytes[pixelIndex++] & 0xFF;
                        int green = imageBytes[pixelIndex++] & 0xFF;
                        int blue = imageBytes[pixelIndex++] & 0xFF;

                        int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                        image.getPixelWriter().setArgb(x, y, argb);
                    } else {
                        break;
                    }
                }
            }

            System.out.println("DEBUG: Image créée au format custom ARGB");
            return image;
        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion des bytes en image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convertir une chaîne Base64 en Image JavaFX
     * @param base64String La chaîne Base64
     * @return L'image JavaFX
     */
    public static Image base64ToImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            return bytesToImage(decodedBytes);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur lors du décodage de la chaîne Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vérifier si le Canvas contient une signature (pixels non-blancs)
     * @param canvas Le Canvas à vérifier
     * @return true si le Canvas contient une signature, false sinon
     */
    public static boolean canvasHasSignature(Canvas canvas) {
        try {
            Image snapshot = captureCanvasImage(canvas);
            if (snapshot == null) {
                return false;
            }

            PixelReader reader = snapshot.getPixelReader();
            int width = (int) snapshot.getWidth();
            int height = (int) snapshot.getHeight();

            // Vérifier s'il y a au moins quelques pixels non-blancs
            int nonWhitePixels = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = reader.getArgb(x, y);
                    int alpha = (color >> 24) & 0xFF;
                    int red = (color >> 16) & 0xFF;
                    int green = (color >> 8) & 0xFF;
                    int blue = color & 0xFF;

                    // Pixel blanc = (255, 255, 255) avec alpha
                    // Vérifier si c'est différent du blanc
                    if (red < 250 || green < 250 || blue < 250) {
                        nonWhitePixels++;
                    }
                }
            }

            // Considérer qu'il y a une signature s'il y a au moins 100 pixels non-blancs
            System.out.println("DEBUG: Pixels non-blancs détectés: " + nonWhitePixels);
            return nonWhitePixels > 100;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de la signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtenir la taille d'une image en bytes (avec formatage lisible)
     * @param imageBytes Le tableau de bytes
     * @return La taille formatée (ex: "2.5 KB")
     */
    public static String formatImageSize(byte[] imageBytes) {
        if (imageBytes == null) {
            return "0 bytes";
        }

        long bytes = imageBytes.length;
        if (bytes <= 0) return "0 bytes";
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}

