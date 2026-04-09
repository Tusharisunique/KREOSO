// IngestionService — orchestrates the full pipeline: parse → chunk → save to PostgreSQL
package com.seekb.ingestion.service;

import com.seekb.ingestion.model.Chunk;
import com.seekb.ingestion.parser.AbstractDocumentParser;
import com.seekb.ingestion.parser.ParserFactory;
import com.seekb.ingestion.repository.ChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.List;

@Service
public class IngestionService {

    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    private ChunkRepository chunkRepository;

    public int ingest(String filename, Path filePath) throws Exception {
        // ... (existing logic)
        AbstractDocumentParser parser = ParserFactory.getParser(filename);
        String rawText = parser.parse(filePath);
        List<String> textChunks = chunkingService.chunk(rawText);

        for (String text : textChunks) {
            Chunk chunk = new Chunk(filename, text);
            chunkRepository.save(chunk);
        }

        return textChunks.size();
    }

    public List<String> getLearnedDocuments() {
        return chunkRepository.findDistinctDocumentNames();
    }

    public void deleteDocument(String documentName) {
        chunkRepository.deleteByDocumentName(documentName);
    }
}
