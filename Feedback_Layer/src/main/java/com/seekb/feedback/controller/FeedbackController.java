package com.seekb.feedback.controller;

import com.seekb.feedback.model.Feedback;
import com.seekb.feedback.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveFeedback(@RequestBody Map<String, Object> data) {
        try {
            String query = (String) data.get("query");
            String answer = (String) data.get("answer");
            int rating = (int) data.get("rating");
            int confidence = (int) data.get("confidence");

            Feedback fb = new Feedback(query, answer, rating, confidence);
            feedbackRepository.save(fb);

            return ResponseEntity.ok(Map.of("message", "Feedback recorded successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
