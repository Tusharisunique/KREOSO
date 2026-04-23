-- Supabase / PostgreSQL Schema for SE-EKB Knowledge Brain
-- Enables vector storage (pgvector) for AI Similarity Search
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_name TEXT,
    text TEXT,
    embedding vector(768),
    status TEXT DEFAULT 'PENDING',
    confidence DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS feedbacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query TEXT,
    answer TEXT,
    rating INT, -- 1 for thumbs up, -1 for thumbs down
    ai_confidence INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON chunks USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);
