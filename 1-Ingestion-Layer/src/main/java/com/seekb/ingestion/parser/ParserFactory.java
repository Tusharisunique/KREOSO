// ParserFactory — returns the correct AbstractDocumentParser subclass based on file extension
package com.seekb.ingestion.parser;

public class ParserFactory {

    public static AbstractDocumentParser getParser(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return new PDFParser();
        } else if (lower.endsWith(".docx")) {
            return new DOCXParser();
        } else if (lower.endsWith(".txt")) {
            return new TextParser();
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }
}
