// TextParser — reads a plain .txt file and returns its full content as a string
package com.seekb.ingestion.parser;

import java.nio.file.Files;
import java.nio.file.Path;

public class TextParser extends AbstractDocumentParser {

    @Override
    public String parse(Path file) throws Exception {
        return Files.readString(file);
    }
}
