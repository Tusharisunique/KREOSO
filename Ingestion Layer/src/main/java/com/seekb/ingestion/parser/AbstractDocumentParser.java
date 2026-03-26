// Abstract base class for all document parsers — enforces the parse() contract via runtime polymorphism
package com.seekb.ingestion.parser;

import java.nio.file.Path;

public abstract class AbstractDocumentParser {

    // Subclasses must implement this to extract raw text from their specific file format
    public abstract String parse(Path file) throws Exception;
}
