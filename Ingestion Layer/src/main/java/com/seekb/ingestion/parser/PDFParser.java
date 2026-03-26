// PDFParser — extracts plain text from a PDF file using Apache PDFBox
package com.seekb.ingestion.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.nio.file.Path;

public class PDFParser extends AbstractDocumentParser {

    @Override
    public String parse(Path file) throws Exception {
        try (PDDocument doc = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
