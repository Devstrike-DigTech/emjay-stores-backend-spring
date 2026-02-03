-- V7__create_staff_profiles_table.sql
-- Staff Management System - Staff Profiles

-- Create enum for employment type
CREATE TYPE employment_type AS ENUM ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN');

-- Create enum for staff status
CREATE TYPE staff_status AS ENUM ('ACTIVE', 'ON_LEAVE', 'SUSPENDED', 'TERMINATED');

-- Staff profiles table
CREATE TABLE staff_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    phone_number VARCHAR(20),
    personal_email VARCHAR(100),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Nigeria',
    nationality VARCHAR(100),

    -- Employment Details
    position VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    employment_type employment_type NOT NULL DEFAULT 'FULL_TIME',
    hire_date DATE NOT NULL,
    contract_end_date DATE,

    -- Compensation
    salary DECIMAL(12, 2),
    bank_name VARCHAR(100),
    account_number VARCHAR(20),
    account_name VARCHAR(100),

    -- Emergency Contact
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),

    -- Status
    status staff_status NOT NULL DEFAULT 'ACTIVE',

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to users table
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_staff_user_id ON staff_profiles(user_id);
CREATE INDEX idx_staff_employee_id ON staff_profiles(employee_id);
CREATE INDEX idx_staff_department ON staff_profiles(department);
CREATE INDEX idx_staff_status ON staff_profiles(status);
CREATE INDEX idx_staff_hire_date ON staff_profiles(hire_date);

-- Comments
COMMENT ON TABLE staff_profiles IS 'Extended profile information for staff members';
COMMENT ON COLUMN staff_profiles.employee_id IS 'Unique employee identifier (e.g., EMP001)';
COMMENT ON COLUMN staff_profiles.salary IS 'Monthly or annual salary';
COMMENT ON COLUMN staff_profiles.contract_end_date IS 'End date for contract employees';