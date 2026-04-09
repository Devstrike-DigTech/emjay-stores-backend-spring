-- V11__create_leave_management_tables.sql
-- Leave Management System

-- Create enum for leave types
CREATE TYPE leave_type AS ENUM ('ANNUAL', 'SICK', 'EMERGENCY', 'UNPAID', 'MATERNITY', 'PATERNITY', 'COMPASSIONATE', 'STUDY');

-- Create enum for leave request status
CREATE TYPE leave_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED');

-- Leave balances table (tracks available leave days)
CREATE TABLE leave_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_profile_id UUID NOT NULL,
    leave_type leave_type NOT NULL,
    year INTEGER NOT NULL,

    -- Balance tracking
    total_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    used_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    pending_days DECIMAL(5, 2) NOT NULL DEFAULT 0,
    available_days DECIMAL(5, 2) GENERATED ALWAYS AS (total_days - used_days - pending_days) STORED,

    -- Carry over
    carried_over_days DECIMAL(5, 2) DEFAULT 0,

    -- Settings
    allow_negative BOOLEAN DEFAULT false,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_leave_balance_staff FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,

    -- Unique constraint: one balance per staff per leave type per year
    CONSTRAINT uq_staff_leave_year UNIQUE (staff_profile_id, leave_type, year),

    -- Constraints
    CONSTRAINT chk_total_days CHECK (total_days >= 0),
    CONSTRAINT chk_used_days CHECK (used_days >= 0),
    CONSTRAINT chk_pending_days CHECK (pending_days >= 0)
);

-- Leave requests table
CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_profile_id UUID NOT NULL,
    leave_balance_id UUID,

    -- Request details
    leave_type leave_type NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(5, 2) NOT NULL,
    reason TEXT,

    -- Status
    status leave_request_status NOT NULL DEFAULT 'PENDING',

    -- Approval tracking
    requested_by UUID NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,

    -- Supporting documents
    supporting_document_url VARCHAR(500),

    -- Notes
    staff_notes TEXT,
    manager_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_leave_request_staff FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_request_balance FOREIGN KEY (leave_balance_id)
        REFERENCES leave_balances(id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_requested_by FOREIGN KEY (requested_by)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_reviewed_by FOREIGN KEY (reviewed_by)
        REFERENCES users(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_leave_total_days CHECK (total_days > 0)
);

-- Leave policies table (company-wide leave policies)
CREATE TABLE leave_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    leave_type leave_type NOT NULL UNIQUE,

    -- Policy details
    policy_name VARCHAR(100) NOT NULL,
    description TEXT,

    -- Allocation
    days_per_year DECIMAL(5, 2) NOT NULL DEFAULT 0,
    max_consecutive_days INTEGER,
    min_days_notice INTEGER DEFAULT 0,

    -- Carryover
    allow_carryover BOOLEAN DEFAULT false,
    max_carryover_days DECIMAL(5, 2) DEFAULT 0,
    carryover_expiry_months INTEGER,

    -- Requirements
    requires_documentation BOOLEAN DEFAULT false,
    requires_manager_approval BOOLEAN DEFAULT true,

    -- Settings
    is_active BOOLEAN DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Leave calendar table (quick lookup for leave dates)
CREATE TABLE leave_calendar (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    leave_request_id UUID NOT NULL,
    staff_profile_id UUID NOT NULL,
    leave_date DATE NOT NULL,
    leave_type leave_type NOT NULL,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_calendar_request FOREIGN KEY (leave_request_id)
        REFERENCES leave_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_calendar_staff FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,

    -- Index for fast date lookups
    CONSTRAINT uq_staff_date UNIQUE (staff_profile_id, leave_date)
);

-- Create indexes
CREATE INDEX idx_leave_balance_staff ON leave_balances(staff_profile_id);
CREATE INDEX idx_leave_balance_year ON leave_balances(year);
CREATE INDEX idx_leave_balance_type ON leave_balances(leave_type);

CREATE INDEX idx_leave_request_staff ON leave_requests(staff_profile_id);
CREATE INDEX idx_leave_request_status ON leave_requests(status);
CREATE INDEX idx_leave_request_dates ON leave_requests(start_date, end_date);
CREATE INDEX idx_leave_request_type ON leave_requests(leave_type);

CREATE INDEX idx_leave_calendar_date ON leave_calendar(leave_date);
CREATE INDEX idx_leave_calendar_staff ON leave_calendar(staff_profile_id);

-- Comments
COMMENT ON TABLE leave_balances IS 'Leave balance tracking per staff per year';
COMMENT ON TABLE leave_requests IS 'Leave requests submitted by staff';
COMMENT ON TABLE leave_policies IS 'Company-wide leave policies and rules';
COMMENT ON TABLE leave_calendar IS 'Flattened view of leave dates for quick lookups';

COMMENT ON COLUMN leave_balances.available_days IS 'Computed: total - used - pending';
COMMENT ON COLUMN leave_balances.carried_over_days IS 'Days carried over from previous year';
COMMENT ON COLUMN leave_requests.total_days IS 'Total leave days requested (including weekends if policy allows)';

-- Insert default leave policies
INSERT INTO leave_policies (leave_type, policy_name, description, days_per_year, allow_carryover, max_carryover_days, requires_manager_approval) VALUES
('ANNUAL', 'Annual Leave', 'Standard annual leave entitlement', 21, true, 5, true),
('SICK', 'Sick Leave', 'Leave for illness or medical appointments', 10, false, 0, false),
('EMERGENCY', 'Emergency Leave', 'Urgent personal matters', 3, false, 0, true),
('UNPAID', 'Unpaid Leave', 'Leave without pay', 0, false, 0, true),
('MATERNITY', 'Maternity Leave', 'Maternity leave for new mothers', 90, false, 0, true),
('PATERNITY', 'Paternity Leave', 'Paternity leave for new fathers', 10, false, 0, true),
('COMPASSIONATE', 'Compassionate Leave', 'Bereavement or family emergencies', 5, false, 0, true),
('STUDY', 'Study Leave', 'Professional development and training', 5, false, 0, true);