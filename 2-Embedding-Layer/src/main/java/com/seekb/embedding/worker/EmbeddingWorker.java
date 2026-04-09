// EmbeddingWorker — Runnable that processes one chunk: embeds it and appends the vector to a shared list
package com.seekb.embedding.worker;

import com.seekb.embedding.client.OllamaClient;
import com.seekb.embedding.model.Chunk;
import java.util.List;

public class EmbeddingWorker implements Runnable {

    private final Chunk chunk;
    private final OllamaClient ollamaClient;
    private final List<float[]> resultList;
    private final List<Chunk> processedChunks;

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
            synchronized (resultList) {
                chunk.setEmbedding(vector);
                resultList.add(vector);
                processedChunks.add(chunk);
            }
        } catch (Exception e) {
            System.err.println("EmbeddingWorker failed for chunk " + chunk.getId() + ": " + e.getMessage());
        }
    }
}
