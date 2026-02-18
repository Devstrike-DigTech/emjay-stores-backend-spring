-- V16__fix_guest_customer_auth_constraint.sql
-- Fix constraint to allow GUEST customers without password

-- Drop the old constraint
ALTER TABLE customers DROP CONSTRAINT IF EXISTS chk_local_auth_password;

-- Add new constraint: LOCAL auth requires password only for REGISTERED customers
ALTER TABLE customers ADD CONSTRAINT chk_local_auth_password
    CHECK (
        (auth_provider = 'LOCAL' AND customer_type = 'REGISTERED' AND password_hash IS NOT NULL) OR
        (auth_provider = 'LOCAL' AND customer_type = 'GUEST' AND password_hash IS NULL) OR
        (auth_provider != 'LOCAL')
    );

-- Comments
COMMENT ON CONSTRAINT chk_local_auth_password ON customers IS
    'LOCAL auth requires password for REGISTERED customers only; GUEST customers do not need passwords';