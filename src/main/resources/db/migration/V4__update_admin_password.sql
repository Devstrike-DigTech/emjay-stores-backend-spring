-- V4__update_admin_password.sql
-- Update admin password to use correct BCrypt hash for Admin@123

UPDATE users
SET password_hash = '$2a$10$cFAy7CDKwLYme6iFo3dXI.ziyGGBLliqbookBoMsF3nvtO8uwoCEC'
WHERE username = 'admin';