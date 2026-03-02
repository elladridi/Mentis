package com.mentalhealth.app.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFService {

    // Custom colors matching your app theme
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(47, 93, 82);      // #2F5D52
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(62, 111, 100);  // #3E6F64
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(155, 199, 181);    // #9BC7B5
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(241, 246, 244);        // #F1F6F4
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(30, 30, 30);          // #1E1E1E
    private static final DeviceRgb TEXT_GRAY = new DeviceRgb(107, 114, 128);       // #6B7280

    /**
     * Generate PDF ticket for a registration
     */
    public static File generateTicketPDF(EventRegistration registration, Event event, String outputDir) {
        try {
            // Create output directory if needed
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "Ticket_" + registration.getId() + "_" + System.currentTimeMillis() + ".pdf";
            File outputFile = new File(dir, fileName);

            PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Add content
            addHeader(document);
            addEventDetails(document, event);
            addTicketDetails(document, registration);
            addQRCode(document, registration, event);
            addFooter(document);

            document.close();
            System.out.println("✅ PDF ticket generated: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            System.err.println("❌ Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void addHeader(Document document) {
        // Main title with colored background
        Table headerTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();

        Cell headerCell = new Cell()
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(20)
                .setBorder(Border.NO_BORDER);

        Paragraph title = new Paragraph("🎟 MENTIS EVENT TICKET")
                .setFontSize(24)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph subtitle = new Paragraph("Mental Health Platform - Event Registration Confirmation")
                .setFontSize(12)
                .setFontColor(ACCENT_COLOR)
                .setTextAlignment(TextAlignment.CENTER);

        headerCell.add(title);
        headerCell.add(subtitle);
        headerTable.addCell(headerCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private static void addEventDetails(Document document, Event event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");

        // Section title
        Paragraph sectionTitle = new Paragraph("📌 EVENT DETAILS")
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Event details table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        table.setBackgroundColor(LIGHT_BG);
        table.setBorder(new SolidBorder(ACCENT_COLOR, 1));

        addDetailRow(table, "Event Name", event.getTitle());
        addDetailRow(table, "Date & Time", event.getDateTime().format(dtf));
        addDetailRow(table, "Location", event.getLocation());
        addDetailRow(table, "Event Type", event.getEventType());
        addDetailRow(table, "Status", event.getStatus());

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addTicketDetails(Document document, EventRegistration registration) {
        String confirmationNumber = String.format("REG-%06d", registration.getId());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        // Section title
        Paragraph sectionTitle = new Paragraph("🎫 TICKET INFORMATION")
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Confirmation number highlight
        Table confirmTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        Cell confirmCell = new Cell()
                .setBackgroundColor(SECONDARY_COLOR)
                .setPadding(15)
                .setBorder(Border.NO_BORDER);

        Paragraph confirmLabel = new Paragraph("Confirmation Number")
                .setFontSize(10)
                .setFontColor(ACCENT_COLOR);
        Paragraph confirmValue = new Paragraph(confirmationNumber)
                .setFontSize(22)
                .setBold()
                .setFontColor(ColorConstants.WHITE);

        confirmCell.add(confirmLabel);
        confirmCell.add(confirmValue);
        confirmTable.addCell(confirmCell);
        document.add(confirmTable);

        // Ticket details table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        table.setBackgroundColor(LIGHT_BG);
        table.setBorder(new SolidBorder(ACCENT_COLOR, 1));

        addDetailRow(table, "Attendee Name", registration.getUserName());
        addDetailRow(table, "Email", registration.getEmail());
        addDetailRow(table, "Phone", registration.getPhone() != null ? registration.getPhone() : "N/A");
        addDetailRow(table, "Ticket Type", registration.getTicketType());
        addDetailRow(table, "Quantity", String.valueOf(registration.getNumberOfTickets()));
        addDetailRow(table, "Total Price", registration.isFreeTicket() ? "FREE" : String.format("$%.2f", registration.getTotalPrice()));
        addDetailRow(table, "Payment Method", registration.getPaymentMethod());
        addDetailRow(table, "Status", registration.getStatus());
        addDetailRow(table, "Registration Date", registration.getRegistrationDate() != null ?
                registration.getRegistrationDate().format(dtf) : "N/A");

        if (registration.getSpecialRequests() != null && !registration.getSpecialRequests().isEmpty()) {
            addDetailRow(table, "Special Requests", registration.getSpecialRequests());
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addQRCode(Document document, EventRegistration registration, Event event) {
        try {
            // Section title
            Paragraph sectionTitle = new Paragraph("📱 QR CODE - SCAN FOR CHECK-IN")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(sectionTitle);

            // Generate QR code
            byte[] qrBytes = QRCodeService.generateQRCodeBytes(registration, event);
            if (qrBytes != null) {
                ImageData imageData = ImageDataFactory.create(qrBytes);
                Image qrImage = new Image(imageData);
                qrImage.setWidth(150);
                qrImage.setHeight(150);
                qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

                // QR code container
                Table qrTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                Cell qrCell = new Cell()
                        .setBackgroundColor(ColorConstants.WHITE)
                        .setBorder(new SolidBorder(ACCENT_COLOR, 2))
                        .setPadding(20)
                        .setTextAlignment(TextAlignment.CENTER);

                qrCell.add(qrImage);
                qrCell.add(new Paragraph("Present this QR code at the entrance")
                        .setFontSize(10)
                        .setFontColor(TEXT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(10));

                qrTable.addCell(qrCell);
                document.add(qrTable);
            }

        } catch (Exception e) {
            System.err.println("Error adding QR code to PDF: " + e.getMessage());
        }

        document.add(new Paragraph("\n"));
    }

    private static void addFooter(Document document) {
        // Divider
        Table divider = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        Cell dividerCell = new Cell()
                .setBackgroundColor(ACCENT_COLOR)
                .setHeight(2)
                .setBorder(Border.NO_BORDER);
        divider.addCell(dividerCell);
        document.add(divider);

        // Footer text
        Paragraph footer = new Paragraph(
                "© 2024 MENTIS - Mental Health Platform\n" +
                        "This ticket is valid for one-time entry only. Please arrive 15 minutes before the event.\n" +
                        "For questions, contact support@mentis.com")
                .setFontSize(9)
                .setFontColor(TEXT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15);

        document.add(footer);
    }

    private static void addDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFontSize(11).setBold().setFontColor(PRIMARY_COLOR))
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A").setFontSize(11).setFontColor(TEXT_DARK))
                .setBorder(Border.NO_BORDER)
                .setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Generate event summary report PDF
     */
    public static File generateEventReportPDF(Event event, java.util.List<EventRegistration> registrations, String outputDir) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "EventReport_" + event.getId() + "_" + System.currentTimeMillis() + ".pdf";
            File outputFile = new File(dir, fileName);

            PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Header
            Paragraph title = new Paragraph("📊 EVENT REPORT")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph eventTitle = new Paragraph(event.getTitle())
                    .setFontSize(18)
                    .setFontColor(SECONDARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(eventTitle);
            document.add(new Paragraph("\n"));

            // Statistics
            int totalRegs = registrations.size();
            int confirmedRegs = (int) registrations.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count();
            int totalTickets = registrations.stream().mapToInt(EventRegistration::getNumberOfTickets).sum();
            double totalRevenue = registrations.stream().filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .mapToDouble(EventRegistration::getTotalPrice).sum();

            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1})).useAllAvailableWidth();
            statsTable.setBackgroundColor(LIGHT_BG);

            addStatCell(statsTable, "Total Registrations", String.valueOf(totalRegs));
            addStatCell(statsTable, "Confirmed", String.valueOf(confirmedRegs));
            addStatCell(statsTable, "Total Tickets", String.valueOf(totalTickets));
            addStatCell(statsTable, "Revenue", String.format("$%.2f", totalRevenue));

            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // Registrations list
            Paragraph listTitle = new Paragraph("📋 All Registrations")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR);
            document.add(listTitle);

            Table regTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 1, 1, 1})).useAllAvailableWidth();

            // Header row
            addTableHeader(regTable, "Name");
            addTableHeader(regTable, "Email");
            addTableHeader(regTable, "Tickets");
            addTableHeader(regTable, "Price");
            addTableHeader(regTable, "Status");

            // Data rows
            for (EventRegistration reg : registrations) {
                addTableCell(regTable, reg.getUserName());
                addTableCell(regTable, reg.getEmail());
                addTableCell(regTable, String.valueOf(reg.getNumberOfTickets()));
                addTableCell(regTable, reg.isFreeTicket() ? "FREE" : String.format("$%.2f", reg.getTotalPrice()));
                addTableCell(regTable, reg.getStatus());
            }

            document.add(regTable);

            document.close();
            System.out.println("✅ Event report generated: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            return null;
        }
    }

    private static void addStatCell(Table table, String label, String value) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        cell.add(new Paragraph(value).setFontSize(22).setBold().setFontColor(PRIMARY_COLOR));
        cell.add(new Paragraph(label).setFontSize(10).setFontColor(TEXT_GRAY));

        table.addCell(cell);
    }

    private static void addTableHeader(Table table, String text) {
        Cell cell = new Cell()
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(8)
                .add(new Paragraph(text).setFontSize(10).setBold().setFontColor(ColorConstants.WHITE));
        table.addCell(cell);
    }

    private static void addTableCell(Table table, String text) {
        Cell cell = new Cell()
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_BG, 1))
                .add(new Paragraph(text != null ? text : "N/A").setFontSize(9).setFontColor(TEXT_DARK));
        table.addCell(cell);
    }
}