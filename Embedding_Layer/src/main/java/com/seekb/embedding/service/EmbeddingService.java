// EmbeddingService — fetches PENDING chunks, runs 4-thread ExecutorService, saves vectors to FAISS flat index
package com.seekb.embedding.service;

import com.seekb.embedding.client.OllamaClient;
import com.seekb.embedding.model.Chunk;
import com.seekb.embedding.repository.ChunkRepository;
import com.seekb.embedding.worker.EmbeddingWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class EmbeddingService {

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private OllamaClient ollamaClient;

    @Scheduled(fixedDelay = 5000)
    public void scheduledEmbed() {
        try {
            embedPending();
        } catch (Exception e) {
            System.err.println("Scheduled embedding failed: " + e.getMessage());
        }
    }

    public int embedPending() throws Exception {
        List<Chunk> pendingChunks = chunkRepository.findByStatus("PENDING");
        if (pendingChunks.isEmpty()) {
            return 0;
        }

        List<float[]> vectors = new ArrayList<>();
        List<Chunk> processedChunks = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (Chunk chunk : pendingChunks) {
            executor.submit(new EmbeddingWorker(chunk, ollamaClient, vectors, processedChunks));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        for (Chunk chunk : processedChunks) {
            chunk.setStatus("COMPLETED");
            chunkRepository.save(chunk);
        }

        return processedChunks.size();
    }
}
