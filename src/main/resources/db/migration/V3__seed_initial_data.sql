-- V3__seed_initial_data.sql
-- Insert initial admin user and sample data

-- Insert default admin user
-- Password: Admin@123 (BCrypt hash)
INSERT INTO users (id, email, username, password_hash, first_name, last_name, role, is_active, is_verified, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'admin@emjay.com',
    'admin',
    '$2a$12$DDmCNnrqzv5jHOTB1k2CmeA205/2OrDu2KI1FDA3I9/I4vP73x1Wy',
    'System',
    'Administrator',
    'ADMIN'::user_role,
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert sample categories
INSERT INTO categories (name, description, parent_id, is_active) VALUES
('Skincare', 'Skin care products and treatments', NULL, true),
('Makeup', 'Cosmetics and makeup products', NULL, true),
('Haircare', 'Hair care products', NULL, true),
('Tools & Accessories', 'Beauty tools and accessories', NULL, true);

-- Insert subcategories for Skincare
INSERT INTO categories (name, description, parent_id, is_active)
SELECT 'Cleansers', 'Face cleansers and washing products', id, true
FROM categories WHERE name = 'Skincare';

INSERT INTO categories (name, description, parent_id, is_active)
SELECT 'Moisturizers', 'Face and body moisturizers', id, true
FROM categories WHERE name = 'Skincare';

INSERT INTO categories (name, description, parent_id, is_active)
SELECT 'Serums', 'Face serums and treatments', id, true
FROM categories WHERE name = 'Skincare';

-- Insert sample supplier
INSERT INTO suppliers (name, contact_person, email, phone, address, is_active)
VALUES (
    'Beauty Supplies Co.',
    'Jane Smith',
    'contact@beautysupplies.com',
    '+234-800-123-4567',
    '123 Beauty Lane, Lagos, Nigeria',
    true
);

-- Note: Sample products can be added after suppliers are created
-- This provides a clean starting point for development