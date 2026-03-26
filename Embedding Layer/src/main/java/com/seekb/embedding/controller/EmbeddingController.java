// EmbeddingController — exposes POST /embed to trigger embedding of all PENDING chunks
package com.seekb.embedding.controller;

import com.seekb.embedding.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/embed")
public class EmbeddingController {

    @Autowired
    private EmbeddingService embeddingService;

    @PostMapping
    public ResponseEntity<String> embed() {
        try {
            int count = embeddingService.embedPending();
            if (count == 0) {
                return ResponseEntity.ok("No PENDING chunks found.");
            }
            return ResponseEntity.ok("Embedded " + count + " chunks. Vectors saved to FAISS index.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Embedding failed: " + e.getMessage());
        }
    }
}
