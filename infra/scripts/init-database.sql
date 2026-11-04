-- ==============================================================================
-- Ordera Multi-Tenant Database Initialization Script
-- ==============================================================================

-- Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Notice log
DO $$
BEGIN
    RAISE NOTICE 'Ordera Database extensions initialized successfully.';
END $$;

