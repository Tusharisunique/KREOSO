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

        // 3. Perform similarity search in Supabase using JdbcTemplate for robust native
        // support
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
        // 1. Greeting Interceptor
        String q = query.toLowerCase().trim();
        if (q.equals("hi") || q.equals("hello") || q.equals("hey") || q.startsWith("how are you")) {
            return Map.of(
                    "answer", "HELLO! I AM KREOSO. HOW CAN I ASSIST YOU TODAY?",
                    "sources", List.of(),
                    "confidence", 100);
        }

        // 2. Retrieval (Search for context)
        List<Chunk> topChunks = search(query, 5); // Increased for better coverage

        String context = "";
        List<String> sources = List.of();

        if (!topChunks.isEmpty()) {
            context = topChunks.stream()
                    .map(Chunk::getText)
                    .collect(Collectors.joining("\n---\n"));

            sources = topChunks.stream()
                    .map(Chunk::getDocumentName)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 3. Generation
        String rawAiResponse = ollamaClient.generateAnswer(query, context);
        
        try {
            // Since we enabled JSON mode in OllamaClient, we can parse directly
            com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(rawAiResponse).getAsJsonObject();
            
            String answer = "";
            if (jsonResponse.has("answer")) answer = jsonResponse.get("answer").getAsString();
            else if (jsonResponse.has("ANSWER")) answer = jsonResponse.get("ANSWER").getAsString();
            
            int confidence = 50;
            if (jsonResponse.has("confidence")) confidence = jsonResponse.get("confidence").getAsInt();
            else if (jsonResponse.has("CONFIDENCE")) confidence = jsonResponse.get("CONFIDENCE").getAsInt();
            
            return Map.of(
                "answer", answer, 
                "sources", sources,
                "confidence", confidence
            );
        } catch (Exception e) {
            // Fallback for any parity issues
            return Map.of(
                "answer", rawAiResponse.trim(), 
                "sources", sources,
                "confidence", 50 
            );
        }
    }
}
