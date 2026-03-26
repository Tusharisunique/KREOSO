// EmbeddingService — fetches PENDING chunks, runs 4-thread ExecutorService, saves vectors to FAISS flat index
package com.seekb.embedding.service;

import com.seekb.embedding.client.OllamaClient;
import com.seekb.embedding.model.Chunk;
import com.seekb.embedding.repository.ChunkRepository;
import com.seekb.embedding.worker.EmbeddingWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
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

    @Value("${faiss.index.path}")
    private String faissIndexPath;

    public int embedPending() throws Exception {
        // Step 1: Fetch all PENDING chunks from PostgreSQL
        List<Chunk> pendingChunks = chunkRepository.findByStatus("PENDING");
        if (pendingChunks.isEmpty()) {
            return 0;
        }

        // Step 2: Shared lists — protected by synchronized in EmbeddingWorker
        List<float[]> vectors = new ArrayList<>();
        List<Chunk> processedChunks = new ArrayList<>();

        // Step 3: 4-thread ExecutorService — one thread per chunk in parallel
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (Chunk chunk : pendingChunks) {
            executor.submit(new EmbeddingWorker(chunk, ollamaClient, vectors, processedChunks));
        }

        // Step 4: Wait for all threads to finish (max 10 minutes)
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        // Step 5: Save all vectors to a local FAISS flat index file (raw float binary format)
        appendToFaissIndex(vectors);

        // Step 6: Update each successfully embedded chunk status to COMPLETED
        for (Chunk chunk : processedChunks) {
            chunk.setStatus("COMPLETED");
            chunkRepository.save(chunk);
        }

        return processedChunks.size();
    }

    // Writes float vectors sequentially to a binary file — compatible with FAISS flat index format
    private void appendToFaissIndex(List<float[]> vectors) throws Exception {
        Path path = Paths.get(faissIndexPath);
        try (OutputStream out = Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (float[] vector : vectors) {
                ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4)
                        .order(ByteOrder.LITTLE_ENDIAN);
                for (float v : vector) {
                    buffer.putFloat(v);
                }
                out.write(buffer.array());
            }
        }
    }
}
