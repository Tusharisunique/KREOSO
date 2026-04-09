// Spring Data JPA repository for Chunk in the Embedding Layer — finds PENDING chunks to process
package com.seekb.embedding.repository;

import com.seekb.embedding.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    List<Chunk> findByStatus(String status);

    @Query(value = "SELECT * FROM chunks WHERE status = 'COMPLETED' ORDER BY embedding <=> CAST(?1 AS vector) LIMIT ?2", nativeQuery = true)
    List<Chunk> findNearestNeighbors(String vectorStr, int limit);
}
