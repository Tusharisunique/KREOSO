// Spring Data JPA repository for Chunk - provides DB access with no boilerplate SQL
package com.seekb.ingestion.repository;

import com.seekb.ingestion.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {
}
