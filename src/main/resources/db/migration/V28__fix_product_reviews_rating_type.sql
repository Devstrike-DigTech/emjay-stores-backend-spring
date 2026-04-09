-- Fix rating column type from SMALLINT (int2) to INTEGER to match Hibernate's mapping for Kotlin Int
ALTER TABLE product_reviews ALTER COLUMN rating TYPE INTEGER;
