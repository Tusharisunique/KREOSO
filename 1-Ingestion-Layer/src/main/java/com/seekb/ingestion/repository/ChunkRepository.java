// Spring Data JPA repository for Chunk - provides DB access with no boilerplate SQL
package com.seekb.ingestion.repository;

import com.seekb.ingestion.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, UUID> {
    
    @Query("SELECT DISTINCT c.documentName FROM Chunk c")
    List<String> findDistinctDocumentNames();

    @Modifying
    @Transactional
    @Query("DELETE FROM Chunk c WHERE c.documentName = :documentName")
    void deleteByDocumentName(String documentName);
}
