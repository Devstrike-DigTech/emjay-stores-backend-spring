#!/bin/bash

# Emjay Backend - Database Setup Script
# This script creates the database user and database with proper permissions

echo "========================================="
echo "Emjay Backend - Database Setup"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if PostgreSQL is running
if ! pg_isready -q; then
    echo -e "${RED}Error: PostgreSQL is not running!${NC}"
    echo "Please start PostgreSQL and try again."
    exit 1
fi

echo "PostgreSQL is running ✓"
echo ""

# Create user if not exists
echo "Creating database user..."
psql -U postgres -c "CREATE USER emjay_dev WITH PASSWORD 'emjay_dev';" 2>/dev/null || echo "User already exists"

# Create database
echo "Creating database..."
psql -U postgres -c "DROP DATABASE IF EXISTS emjay_dev_db;"
psql -U postgres -c "CREATE DATABASE emjay_dev_db OWNER emjay_dev;"

# Grant privileges
echo "Granting privileges..."
psql -U postgres -d emjay_dev_db <<EOF
GRANT ALL ON SCHEMA public TO emjay_dev;
GRANT CREATE ON SCHEMA public TO emjay_dev;
GRANT USAGE ON SCHEMA public TO emjay_dev;
ALTER SCHEMA public OWNER TO emjay_dev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO emjay_dev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO emjay_dev;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO emjay_dev;
EOF

# Verify
echo ""
echo -e "${GREEN}Database setup completed!${NC}"
echo ""
echo "Database details:"
echo "  Name: emjay_dev_db"
echo "  User: emjay_dev"
echo "  Password: emjay_dev"
echo "  Port: 5432"
echo ""
echo "Connection string:"
echo "  jdbc:postgresql://localhost:5432/emjay_dev_db"
echo ""
echo -e "${YELLOW}You can now start the Spring Boot application!${NC}"
