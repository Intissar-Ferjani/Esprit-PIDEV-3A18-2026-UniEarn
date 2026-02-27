package uniearn.services.users.freelancer.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentCardOCRService {

    public OCRResult verifyStudentCard(File imageFile, String registeredName) {
        String extractedText = extractText(imageFile);

        if (extractedText == null || extractedText.isBlank()) {
            return new OCRResult(false, "Could not read text from the image. Please upload a clearer photo.", extractedText);
        }

        System.out.println("=== OCR Extracted Text ===\n" + extractedText + "\n==========================");

        // Normalize once: remove accents, uppercase, collapse whitespace
        // This makes all three checks accent-insensitive
        String normalizedText = normalize(extractedText);

        boolean isStudentCard = isStudentCard(normalizedText, extractedText);
        boolean currentYearValid = isCurrentAcademicYearPresent(extractedText); // keep raw for year digits
        boolean nameMatches = doesNameMatch(normalizedText, registeredName);

        System.out.println("isStudentCard:    " + isStudentCard);
        System.out.println("currentYearValid: " + currentYearValid);
        System.out.println("nameMatches:      " + nameMatches);

        if (!isStudentCard) {
            return new OCRResult(false,
                    "This does not appear to be a student card. Please upload your official student card.",
                    extractedText);
        }

        if (!currentYearValid) {
            return new OCRResult(false,
                    "Your card must be valid for the current academic year (" + getCurrentAcademicYearString() + "). Please upload a current card.",
                    extractedText);
        }

        if (!nameMatches) {
            return new OCRResult(false,
                    "The name on the card does not match your registered name (" + registeredName + "). Please upload your own student card.",
                    extractedText);
        }

        return new OCRResult(true,
                "Student card verified — card, academic year, and name all confirmed.",
                extractedText);
    }

    // ── Text extraction ────────────────────────────────────────────────────────

    private String extractText(File imageFile) {
        try {
            Tesseract tesseract = new Tesseract();
            String tessDataPath = new File("tessdata").getAbsolutePath();
            System.out.println("Tessdata path: " + tessDataPath);
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("fra+eng+ara");
            tesseract.setPageSegMode(3);
            tesseract.setOcrEngineMode(1);
            tesseract.setTessVariable("user_defined_dpi", "300");

            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) {
                System.err.println("ImageIO could not read the file: " + imageFile.getName());
                return null;
            }
            return tesseract.doOCR(bufferedImage);

        } catch (TesseractException e) {
            System.err.println("OCR Tesseract error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("OCR IO error: " + e.getMessage());
            return null;
        }
    }

    // ── Normalizer ─────────────────────────────────────────────────────────────

    /**
     * Strips accents (É→E, è→e, etc.), uppercases, and collapses whitespace.
     * This lets all checks work regardless of OCR accent detection.
     */
    private String normalize(String text) {
        if (text == null) return "";
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Remove combining diacritical marks (accents)
        String stripped = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        return stripped.toUpperCase().replaceAll("\\s+", " ").trim();
    }

    // ── Validator 1: Is it a student card? ────────────────────────────────────

    private boolean isStudentCard(String normalizedText, String rawText) {
        // Full phrase match (ideal case, after accent normalization É→E)
        if (normalizedText.contains("CARTE ETUDIANT")
                || normalizedText.contains("CARTE D ETUDIANT")
                || normalizedText.contains("CARTE D'ETUDIANT")) {
            System.out.println("✓ Student card keyword matched (full phrase)");
            return true;
        }

        // OCR often mangles "CARTE ÉTUDIANT" — the É becomes garbage or is dropped,
        // leaving only "TE ETUDIANT" or just "ETUDIANT".
        // We accept "ETUDIANT" alone since year + name are validated separately.
        if (normalizedText.contains("ETUDIANT")) {
            System.out.println("✓ Student card keyword matched (ETUDIANT partial — OCR mangled CARTE)");
            return true;
        }

        // Arabic equivalents
        if (rawText.contains("بطاقة") || rawText.contains("طالب")) {
            System.out.println("✓ Student card keyword matched (Arabic)");
            return true;
        }

        // Additional university/card signals
        if (normalizedText.contains("ANNEE UNIVERSITAIRE")
                || normalizedText.contains("IDENTIFIANT")
                || normalizedText.contains("HONORIS")) {
            System.out.println("✓ Student card keyword matched (university context signals)");
            return true;
        }

        System.out.println("✗ No student card keyword found. Normalized text snippet: "
                + normalizedText.substring(0, Math.min(200, normalizedText.length())));
        return false;
    }

    // ── Validator 2: Current academic year ────────────────────────────────────

    private boolean isCurrentAcademicYearPresent(String text) {
        int currentYear = LocalDate.now().getYear();
        int academicYearStart = LocalDate.now().getMonthValue() >= 9
                ? currentYear
                : currentYear - 1;
        int academicYearEnd = academicYearStart + 1;

        System.out.println("Expecting academic year: " + academicYearStart + "/" + academicYearEnd);

        // Match separators: - / \ – (en-dash) and even a space or nothing
        Pattern fullYear = Pattern.compile("(20\\d{2})[/\\\\\\-–\\s]?(20\\d{2})");
        Matcher m = fullYear.matcher(text);
        while (m.find()) {
            int y1 = Integer.parseInt(m.group(1));
            int y2 = Integer.parseInt(m.group(2));
            if ((y1 == academicYearStart && y2 == academicYearEnd)
                    || (y1 == academicYearEnd && y2 == academicYearStart)) {
                System.out.println("✓ Current academic year matched: " + m.group(0));
                return true;
            }
        }

        System.out.println("✗ Current academic year " + academicYearStart + "-" + academicYearEnd + " not found.");
        return false;
    }

    // ── Validator 3: Name match ────────────────────────────────────────────────

    private boolean doesNameMatch(String normalizedText, String registeredName) {
        if (registeredName == null || registeredName.isBlank()) return true;

        // Normalize the registered name the same way
        String normalizedName = normalize(registeredName).trim();

        // Try full name match
        if (normalizedText.contains(normalizedName)) {
            System.out.println("✓ Full name matched: " + normalizedName);
            return true;
        }

        // Partial: each word of the name that is >2 chars is checked individually.
        // At least ceil(n/2) parts must appear.
        String[] nameParts = normalizedName.split("\\s+");
        int matchCount = 0;
        for (String part : nameParts) {
            if (part.length() > 2 && normalizedText.contains(part)) {
                System.out.println("  ✓ Name part matched: " + part);
                matchCount++;
            }
        }

        int required = (int) Math.ceil(nameParts.length / 2.0);
        boolean partialMatch = matchCount >= required;

        if (partialMatch) {
            System.out.println("✓ Partial name matched: " + matchCount + "/" + nameParts.length + " parts (required: " + required + ")");
        } else {
            System.out.println("✗ Name mismatch. Looking for: " + normalizedName
                    + " | Matched " + matchCount + "/" + nameParts.length + " parts (required: " + required + ")");
        }
        return partialMatch;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getCurrentAcademicYearString() {
        int currentYear = LocalDate.now().getYear();
        int start = LocalDate.now().getMonthValue() >= 9 ? currentYear : currentYear - 1;
        return start + "-" + (start + 1);
    }

    // ── Result ────────────────────────────────────────────────────────────────

    public static class OCRResult {
        private final boolean verified;
        private final String message;
        private final String rawText;

        public OCRResult(boolean verified, String message, String rawText) {
            this.verified = verified;
            this.message = message;
            this.rawText = rawText;
        }

        public boolean isVerified() { return verified; }
        public String getMessage()  { return message; }
        public String getRawText()  { return rawText; }
    }
}