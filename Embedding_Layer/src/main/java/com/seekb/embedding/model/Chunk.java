// Chunk entity shared with Ingestion Layer — Embedding Layer reads PENDING chunks and updates status
package com.seekb.embedding.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    private UUID id;

    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String status;

    private double confidence;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    private LocalDateTime createdAt;

    public Chunk() {}

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

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
