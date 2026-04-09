package com.seekb.embedding.controller;

import com.seekb.embedding.model.Chunk;
import com.seekb.embedding.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/query")
@CrossOrigin(origins = "*") // For frontend accessibility
public class QueryController {

    @Autowired
    private SearchService searchService;

    /**
     * Search for similar text chunks based on a query.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Chunk>> search(@RequestParam("q") String query,
                                             @RequestParam(value = "limit", defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(searchService.search(query, limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ask a question and get a generated answer from the "Brain".
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty."));
        }

        try {
            Map<String, Object> result = searchService.ask(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }
}
