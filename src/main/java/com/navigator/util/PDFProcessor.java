package com.navigator.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for processing PDF files.
 * Uses Apache PDFBox 3.x for text extraction.
 */
@Slf4j
public class PDFProcessor {

    /**
     * Extract text from a PDF file
     */
    public static String extractText(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extract text with metadata from a PDF file
     */
    public static Map<String, Object> extractTextWithMetadata(File pdfFile) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            result.put("text", text);
            result.put("filename", pdfFile.getName());
            result.put("pages", document.getNumberOfPages());
            result.put("path", pdfFile.getAbsolutePath());

            // Add document info if available
            if (document.getDocumentInformation() != null) {
                result.put("title", document.getDocumentInformation().getTitle());
                result.put("author", document.getDocumentInformation().getAuthor());
                result.put("subject", document.getDocumentInformation().getSubject());
            }
        }

        return result;
    }

    /**
     * Extract text and split into chunks
     */
    public static java.util.List<String> extractAndChunk(File pdfFile, int chunkSize, int overlap)
            throws IOException {
        String text = extractText(pdfFile);
        TextSplitter splitter = new TextSplitter(chunkSize, overlap);
        return splitter.splitText(text);
    }
}
