package com.clinicaltrial.assistant.controller;

import com.clinicaltrial.assistant.model.ClinicalEvent;
import com.clinicaltrial.assistant.repository.ClinicalEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/clinical-events")
@RequiredArgsConstructor
public class SourceDocumentController {
    private final ClinicalEventRepository eventRepo;

    @GetMapping(value = "/{id}/source-document", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> sourceDocument(@PathVariable Long id) throws Exception {
        ClinicalEvent event = eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Clinical event not found"));
        if (!"LAB_PDF".equalsIgnoreCase(event.getSourceFormat())) throw new RuntimeException("This source is not a PDF document");
        byte[] pdf = createPdf(event);
        String filename = event.getSourceDocumentName() != null ? event.getSourceDocumentName() : "clinical-source-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename.replace("\"", "") + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    private byte[] createPdf(ClinicalEvent event) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(); document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = 740;
                stream.setNonStrokingColor(15, 23, 42);
                stream.beginText(); stream.setFont(PDType1Font.HELVETICA_BOLD, 20); stream.newLineAtOffset(54, y); stream.showText("ClinicalTrialIQ Laboratory Source"); stream.endText(); y -= 32;
                stream.setNonStrokingColor(37, 99, 235);
                stream.beginText(); stream.setFont(PDType1Font.HELVETICA_BOLD, 10); stream.newLineAtOffset(54, y); stream.showText("VERIFIED SOURCE DOCUMENT  |  " + safe(event.getSourceSystem()) + "  |  " + safe(event.getSourceRecordId())); stream.endText(); y -= 28;
                stream.setNonStrokingColor(51, 65, 85);
                for (String line : wrap(event.getRawSourceText() != null ? event.getRawSourceText() : "Source text unavailable", 86)) {
                    if (y < 55) break;
                    stream.beginText(); stream.setFont(PDType1Font.COURIER, 10); stream.newLineAtOffset(54, y); stream.showText(safe(line)); stream.endText(); y -= 15;
                }
                stream.setNonStrokingColor(100, 116, 139);
                stream.beginText(); stream.setFont(PDType1Font.HELVETICA, 8); stream.newLineAtOffset(54, 35); stream.showText("Synthetic research record • Generated from preserved source content • Event ID " + event.getId()); stream.endText();
            }
            document.save(output); return output.toByteArray();
        }
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.replace("\r", "").split("\n", -1)) {
            if (paragraph.isBlank()) { lines.add(""); continue; }
            String remaining = paragraph;
            while (remaining.length() > width) { int cut = remaining.lastIndexOf(' ', width); if (cut < 1) cut = width; lines.add(remaining.substring(0, cut)); remaining = remaining.substring(cut).trim(); }
            lines.add(remaining);
        }
        return lines;
    }
    private String safe(String value) { return value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "-"); }
}
