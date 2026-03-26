// DOCXParser — extracts plain text from a .docx file using Apache POI
package com.seekb.ingestion.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

public class DOCXParser extends AbstractDocumentParser {

    @Override
    public String parse(Path file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file.toFile());
             XWPFDocument doc = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                sb.append(para.getText()).append("\n");
            }
        }
        return sb.toString();
    }
}
