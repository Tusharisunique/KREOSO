// Spring Data JPA repository for Chunk in the Embedding Layer — finds PENDING chunks to process
package com.seekb.embedding.repository;

import com.seekb.embedding.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {

    List<Chunk> findByStatus(String status);
}
