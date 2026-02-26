package services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CVParserService {

    /**
     * Extract text from CV file (PDF or DOCX)
     */
    public String extractTextFromCV(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractFromPDF(file);
        } else if (fileName.endsWith(".docx")) {
            return extractFromDOCX(file);
        } else {
            throw new IOException("Unsupported file format. Please upload PDF or DOCX.");
        }
    }

    private String extractFromPDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }
}