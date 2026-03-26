// EmbeddingWorker — Runnable that processes one chunk: embeds it and appends the vector to a shared list
package com.seekb.embedding.worker;

import com.seekb.embedding.client.OllamaClient;
import com.seekb.embedding.model.Chunk;
import java.util.List;

public class EmbeddingWorker implements Runnable {

    private final Chunk chunk;
    private final OllamaClient ollamaClient;
    private final List<float[]> resultList;   // shared across threads — callers must synchronize
    private final List<Chunk> processedChunks; // tracks which chunks were embedded successfully

    public EmbeddingWorker(Chunk chunk, OllamaClient ollamaClient,
                           List<float[]> resultList, List<Chunk> processedChunks) {
        this.chunk = chunk;
        this.ollamaClient = ollamaClient;
        this.resultList = resultList;
        this.processedChunks = processedChunks;
    }

    @Override
    public void run() {
        try {
            float[] vector = ollamaClient.embed(chunk.getText());

            // Synchronized write to shared lists — prevents race conditions from multi-threading
            synchronized (resultList) {
                resultList.add(vector);
                processedChunks.add(chunk);
            }
        } catch (Exception e) {
            System.err.println("EmbeddingWorker failed for chunk " + chunk.getId() + ": " + e.getMessage());
        }
    }
}
