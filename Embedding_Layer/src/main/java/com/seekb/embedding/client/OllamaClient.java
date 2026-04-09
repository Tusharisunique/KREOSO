// OllamaClient - sends chunk text to Ollama's REST API and returns a float[768] embedding vector
package com.seekb.embedding.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class OllamaClient {

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    private static final String EMBED_MODEL = "nomic-embed-text";
    private static final String CHAT_MODEL = "llama3.1:8b";

    public float[] embed(String text) throws Exception {
        URL url = new URL(ollamaBaseUrl + "/api/embeddings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build JSON request body
        JsonObject body = new JsonObject();
        body.addProperty("model", EMBED_MODEL);
        body.addProperty("prompt", text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Parse embedding array from JSON
        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray embeddingArray = json.getAsJsonArray("embedding");

        float[] vector = new float[embeddingArray.size()];
        for (int i = 0; i < embeddingArray.size(); i++) {
            vector[i] = embeddingArray.get(i).getAsFloat();
        }

        return vector;
    }

    public String generateAnswer(String query, String context) throws Exception {
        URL url = new URL(ollamaBaseUrl + "/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String prompt = "Context:\n" + context + "\n\nQuestion: " + query + "\n\nAnswer based purely on the context above. If the context doesn't contain the answer, say you don't know.";

        JsonObject body = new JsonObject();
        body.addProperty("model", CHAT_MODEL);
        body.addProperty("prompt", prompt);
        body.addProperty("stream", false);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        return json.get("response").getAsString();
    }
}
