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

    private static final String MODEL = "nomic-embed-text";

    public float[] embed(String text) throws Exception {
        URL url = new URL(ollamaBaseUrl + "/api/embeddings");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build JSON request body
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
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
}
