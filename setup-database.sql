-- Emjay Backend Database Setup Script
-- Run this as PostgreSQL superuser (postgres)

-- ============================================
-- Development Database Setup
-- ============================================

-- Drop existing database if needed (CAUTION: This deletes all data)
-- DROP DATABASE IF EXISTS emjay_dev_db;

-- Create database with proper owner
CREATE DATABASE emjay_dev_db OWNER emjay_dev;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE emjay_dev_db TO emjay_dev;

-- Connect to the database
\c emjay_dev_db

-- Grant schema privileges (PostgreSQL 15+ requirement)
GRANT ALL ON SCHEMA public TO emjay_dev;
GRANT CREATE ON SCHEMA public TO emjay_dev;
GRANT USAGE ON SCHEMA public TO emjay_dev;

-- Set schema owner
ALTER SCHEMA public OWNER TO emjay_dev;

-- Grant default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO emjay_dev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO emjay_dev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO emjay_dev;

-- Verify permissions
\dp

-- Show current database info
\l+ emjay_dev_db

-- ============================================
-- Production Database Setup (uncomment when needed)
-- ============================================

-- CREATE DATABASE emjay_db OWNER emjay_user;
-- GRANT ALL PRIVILEGES ON DATABASE emjay_db TO emjay_user;
-- \c emjay_db
-- GRANT ALL ON SCHEMA public TO emjay_user;
-- ALTER SCHEMA public OWNER TO emjay_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO emjay_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO emjay_user;

\echo 'Database setup completed successfully!'
