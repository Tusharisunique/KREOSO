-- Supabase / PostgreSQL Schema for SE-EKB Knowledge Brain
-- Enables vector storage (pgvector) for AI Similarity Search

-- 1. Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Create chunks table
-- Stores the original text and its mathematical embedding
CREATE TABLE IF NOT EXISTS chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_name TEXT,
    text TEXT,
    embedding vector(768), -- Dimension 768 is for 'nomic-embed-text'
    status TEXT DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED
    confidence DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create HNSW index for high-speed similarity search
-- Adjust 'm' and 'ef_construction' for performance vs accuracy
CREATE INDEX ON chunks USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- 4. Workflow Notes:
-- - Ingestion Layer (8081) inserts PENDING chunks.
-- - Embedding Layer (8082) updates embedding and marks COMPLETED.
-- - Query & Reasoning (8083) performs the <=> similarity search.
