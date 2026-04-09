package com.seekb.embedding.service;

import com.seekb.embedding.client.OllamaClient;
import com.seekb.embedding.model.Chunk;
import com.seekb.embedding.repository.ChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OllamaClient ollamaClient;

    /**
     * Finds the most relevant chunks for a given query string.
     */
    public List<Chunk> search(String query, int limit) throws Exception {
        // 1. Embed the user's query
        float[] queryVector = ollamaClient.embed(query);

        // 2. Convert float[] to PGVector string format: [0.1,0.2,...]
        String vectorStr = Arrays.toString(queryVector).replace(" ", "");

        // 3. Perform similarity search in Supabase using JdbcTemplate for robust native support
        String sql = "SELECT id, document_name, text, status, confidence, created_at FROM chunks " +
                     "WHERE status = 'COMPLETED' " +
                     "ORDER BY embedding <=> CAST(? AS vector) LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Chunk chunk = new Chunk();
            chunk.setId(java.util.UUID.fromString(rs.getString("id")));
            chunk.setDocumentName(rs.getString("document_name"));
            chunk.setText(rs.getString("text"));
            chunk.setStatus(rs.getString("status"));
            chunk.setConfidence(rs.getDouble("confidence"));
            // Note: embedding column (vector) is ignored in the mapping to avoid complexity
            return chunk;
        }, vectorStr, limit);
    }

    /**
     * Performs RAG: Retrieves context and generates an answer using local LLM.
     */
    public Map<String, Object> ask(String query) throws Exception {
        List<Chunk> topChunks = search(query, 3);
        
        if (topChunks.isEmpty()) {
            return Map.of("answer", "I'm sorry, I couldn't find any information in the knowledge brain related to your question.", 
                          "sources", List.of());
        }

        String context = topChunks.stream()
                .map(Chunk::getText)
                .collect(Collectors.joining("\n---\n"));

        List<String> sources = topChunks.stream()
                .map(Chunk::getDocumentName)
                .distinct()
                .collect(Collectors.toList());

        String answer = ollamaClient.generateAnswer(query, context);
        
        return Map.of("answer", answer, "sources", sources);
    }
}
