package com.seekb.ui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.util.List;
import java.util.Map;

@Controller
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ingestion.url}")
    private String ingestionUrl;

    @Value("${embedding.url}")
    private String embeddingUrl;

    @Value("${reasoning.url}")
    private String reasoningUrl;

    // --- Authentication ---

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if ("admin".equals(username) && "admin@123".equals(password)) {
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin";
        } else if ("user".equals(username)) {
            session.setAttribute("role", "USER");
            return "redirect:/chat";
        }
        model.addAttribute("error", true);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // --- Admin Views ---

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        
        try {
            List documents = restTemplate.getForObject(ingestionUrl + "/ingest/all", List.class);
            model.addAttribute("documents", documents);
        } catch (Exception e) {
            model.addAttribute("message", "Error connecting to Ingestion Layer: " + e.getMessage());
        }
        return "admin";
    }

    @PostMapping("/admin/upload")
    public String upload(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";

        try {
            // Save to temp file
            File tempFile = File.createTempFile("ui_", file.getOriginalFilename());
            file.transferTo(tempFile);

            // Forward to Ingestion Layer
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(ingestionUrl + "/ingest", requestEntity, String.class);
            
            model.addAttribute("message", "Success: " + response);
            tempFile.delete();
        } catch (Exception e) {
            model.addAttribute("message", "Failure: " + e.getMessage());
        }
        
        return admin(session, model);
    }

    @PostMapping("/admin/delete")
    public String delete(@RequestParam String name, HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return "redirect:/";
        
        try {
            restTemplate.delete(ingestionUrl + "/ingest/" + name);
            model.addAttribute("message", "Document forgotten: " + name);
        } catch (Exception e) {
            model.addAttribute("message", "Deletion failed: " + e.getMessage());
        }
        
        return admin(session, model);
    }

    // --- User Views ---

    @GetMapping("/chat")
    public String chat(HttpSession session) {
        if (session.getAttribute("role") == null) return "redirect:/";
        return "chat";
    }

    @PostMapping("/chat/ask")
    @ResponseBody
    public Map ask(@RequestBody Map<String, String> request, HttpSession session) {
        if (session.getAttribute("role") == null) return Map.of("answer", "Session Expired.");
        return restTemplate.postForObject(reasoningUrl + "/query/ask", request, Map.class);
    }
}
