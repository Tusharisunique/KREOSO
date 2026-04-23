package com.seekb.feedback.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String query;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private int rating; // 1 for thumbs up, -1 for thumbs down

    @Column(name = "ai_confidence")
    private int aiConfidence;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Feedback() {}

    public Feedback(String query, String answer, int rating, int aiConfidence) {
        this.query = query;
        this.answer = answer;
        this.rating = rating;
        this.aiConfidence = aiConfidence;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public int getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(int aiConfidence) { this.aiConfidence = aiConfidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
