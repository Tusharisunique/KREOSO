// Chunk entity — represents a text segment parsed from an uploaded document, stored in PostgreSQL
package com.seekb.ingestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String text;

    // PENDING → being processed, COMPLETED → embedding done
    private String status;

    private double confidence;

    private LocalDateTime createdAt;

    public Chunk() {}

    public Chunk(String documentName, String text) {
        this.documentName = documentName;
        this.text = text;
        this.status = "PENDING";
        this.confidence = 1.0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
