// ChunkingService — splits raw text into overlapping chunks of ~750 chars with 50-char overlap
package com.seekb.ingestion.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 750;
    private static final int OVERLAP = 50;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(text.substring(start, end));
            start += (CHUNK_SIZE - OVERLAP);
        }

        return chunks;
    }
}
