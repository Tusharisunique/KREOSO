// IngestionController — exposes POST /ingest endpoint to accept file uploads
package com.seekb.ingestion.controller;

import com.seekb.ingestion.service.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/ingest")
@CrossOrigin(origins = "*") // For frontend accessibility
public class IngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file) {
        // ... (existing logic)
        try {
            Path tempFile = Files.createTempFile("seekb_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            int count = ingestionService.ingest(file.getOriginalFilename(), tempFile);
            Files.deleteIfExists(tempFile);
            return ResponseEntity.ok("Ingested " + count + " chunks from: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<String>> getAllDocuments() {
        return ResponseEntity.ok(ingestionService.getLearnedDocuments());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteDocument(@PathVariable String name) {
        ingestionService.deleteDocument(name);
        return ResponseEntity.ok("Deleted document and all related chunks: " + name);
    }
}
