package uniearn.services.projet;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import uniearn.model.entities.projet.Project;
import uniearn.model.entities.projet.Task;

import java.io.FileOutputStream;
import java.util.List;

public class PDFService {

    public void generateProjectReport(Project project, List<Task> tasks, String destPath) throws Exception {
        // 1) Initialize Document and Writer for iText 5
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        // 2) Centered title: "Project Report"
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph title = new Paragraph("Project Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 3) Project details
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Project Name: ", boldFont));
        document.add(new Paragraph(project.getTitle() != null ? project.getTitle() : "N/A"));

        document.add(new Paragraph("Description: ", boldFont));
        document.add(new Paragraph(project.getDescription() != null ? project.getDescription() : "N/A"));

        document.add(new Paragraph("Budget: ", boldFont));
        document.add(new Paragraph(String.format("%.2f DT", project.getBudget())));

        // Progress percentage calculation
        double progress = 0;
        if (!tasks.isEmpty()) {
            long doneCount = tasks.stream()
                    .filter(t -> t.getTaskstatus() != null && "DONE".equalsIgnoreCase(t.getTaskstatus().toString()))
                    .count();
            progress = (double) doneCount / tasks.size() * 100;
        }
        document.add(new Paragraph("Progress: ", boldFont));
        document.add(new Paragraph(String.format("%.1f%%", progress)));
        document.add(new Paragraph("\n"));

        // 4) Structured table listing all tasks
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Table Header
        PdfPCell header1 = new PdfPCell(new Phrase("Title", boldFont));
        PdfPCell header2 = new PdfPCell(new Phrase("Status", boldFont));
        PdfPCell header3 = new PdfPCell(new Phrase("Estimated Days", boldFont));

        BaseColor headerBg = new BaseColor(220, 220, 220); // LIGHT_GRAY
        header1.setBackgroundColor(headerBg);
        header2.setBackgroundColor(headerBg);
        header3.setBackgroundColor(headerBg);

        table.addCell(header1);
        table.addCell(header2);
        table.addCell(header3);

        // Table Rows
        for (Task task : tasks) {
            table.addCell(task.getTitle());
            table.addCell(task.getTaskstatus() != null ? task.getTaskstatus().toString() : "N/A");

            // Mapping priority back to estimated days as previously refined
            int days = 3; // Medium or default
            if (task.getPriority() != null) {
                switch (task.getPriority()) {
                    case High:
                        days = 5;
                        break;
                    case Low:
                        days = 1;
                        break;
                    default:
                        days = 3;
                        break;
                }
            }
            table.addCell(String.valueOf(days));
        }

        document.add(table);

        // 5) Close document
        document.close();
        System.out.println("PDF (iText 5) generated at: " + destPath);
    }
}
