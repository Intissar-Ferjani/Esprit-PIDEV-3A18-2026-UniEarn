package uniearn.services.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentCardOCRService {

    private static final String[] KNOWN_UNIVERSITIES = {
            "institut", "superieur", "supérieur", "iset", "isims", "esprit",
            "université", "universite", "faculté", "faculte", "ecole",
            "enit", "ensi", "insat", "supcom", "ihec", "iscae",
            "carte d'etudiant", "etudiant", "élève",
            "student", "university", "faculty", "higher",
            "وزارة", "جامعة", "معهد", "كلية", "طالب"
    };

    public OCRResult verifyStudentCard(File imageFile, String registeredName) {
        String extractedText = extractText(imageFile);

        if (extractedText == null || extractedText.isBlank()) {
            return new OCRResult(false, "Could not read text from the image. Please upload a clearer photo.", extractedText);
        }

        System.out.println("=== OCR Extracted Text ===\n" + extractedText + "\n==========================");

        String upperText = extractedText.toUpperCase();

        // Validator 1: CARTE D'ETUDIANT
        boolean isStudentCard = upperText.contains("CARTE D'ETUDIANT")
                || upperText.contains("CARTE ETUDIANT")
                || extractedText.contains("بطاقة");

        // Validator 2: Current academic year
        boolean currentYearValid = isCurrentAcademicYearPresent(extractedText);

        // Validator 3: Name matches registered name
        boolean nameMatches = doesNameMatch(extractedText, registeredName);

        System.out.println("isStudentCard: " + isStudentCard);
        System.out.println("currentYearValid: " + currentYearValid);
        System.out.println("nameMatches: " + nameMatches);

        if (!isStudentCard) {
            return new OCRResult(false,
                    "This does not appear to be a student card. Please upload your official CARTE D'ETUDIANT.",
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

    private boolean isCurrentAcademicYearPresent(String text) {
        int currentYear = LocalDate.now().getYear();
        int academicYearStart = LocalDate.now().getMonthValue() >= 9
                ? currentYear
                : currentYear - 1;
        int academicYearEnd = academicYearStart + 1;

        System.out.println("Expecting academic year: " + academicYearStart + "-" + academicYearEnd);

        Pattern fullYear = Pattern.compile("(20\\d{2})[/\\\\\\-–](20\\d{2})");
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

    private boolean doesNameMatch(String extractedText, String registeredName) {
        if (registeredName == null || registeredName.isBlank()) return true;

        String upperText = extractedText.toUpperCase();
        String upperName = registeredName.toUpperCase().trim();

        // Try full name match first
        if (upperText.contains(upperName)) {
            System.out.println("✓ Full name matched: " + upperName);
            return true;
        }

        // Try partial: at least half the name parts must appear
        String[] nameParts = upperName.split("\\s+");
        int matchCount = 0;
        for (String part : nameParts) {
            if (part.length() > 2 && upperText.contains(part)) {
                matchCount++;
            }
        }

        boolean partialMatch = nameParts.length > 0 && matchCount >= Math.ceil(nameParts.length / 2.0);
        if (partialMatch) {
            System.out.println("✓ Partial name matched: " + matchCount + "/" + nameParts.length + " parts");
        } else {
            System.out.println("✗ Name mismatch. Looking for: " + upperName);
        }
        return partialMatch;
    }

    private String getCurrentAcademicYearString() {
        int currentYear = LocalDate.now().getYear();
        int start = LocalDate.now().getMonthValue() >= 9 ? currentYear : currentYear - 1;
        return start + "-" + (start + 1);
    }

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