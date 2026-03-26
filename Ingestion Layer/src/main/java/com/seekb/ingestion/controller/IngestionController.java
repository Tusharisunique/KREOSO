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
public class IngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file) {
        try {
            // Save uploaded file to a temp location
            Path tempFile = Files.createTempFile("seekb_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            int count = ingestionService.ingest(file.getOriginalFilename(), tempFile);

            // Clean up temp file
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("Ingested " + count + " chunks from: " + file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
