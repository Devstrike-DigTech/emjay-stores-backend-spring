-- V9__create_shift_scheduling_tables.sql
-- Shift Scheduling System

-- Create enum for shift types
CREATE TYPE shift_type AS ENUM ('MORNING', 'AFTERNOON', 'EVENING', 'NIGHT', 'CUSTOM');

-- Create enum for shift status
CREATE TYPE shift_status AS ENUM ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');

-- Create enum for days of week
CREATE TYPE day_of_week AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');

-- Shift templates table (reusable shift definitions)
CREATE TABLE shift_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    shift_type shift_type NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description TEXT,
    color_code VARCHAR(7) DEFAULT '#3B82F6', -- For calendar UI
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Staff shifts table (actual shift assignments)
CREATE TABLE staff_shifts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_profile_id UUID NOT NULL,
    shift_template_id UUID,

    -- Shift details
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    -- Break time (in minutes)
    break_duration_minutes INTEGER DEFAULT 30,

    -- Status tracking
    status shift_status NOT NULL DEFAULT 'SCHEDULED',

    -- Notes
    notes TEXT,
    assigned_by UUID, -- User who assigned the shift

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_staff_profile FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_shift_template FOREIGN KEY (shift_template_id)
        REFERENCES shift_templates(id) ON DELETE SET NULL,
    CONSTRAINT fk_assigned_by FOREIGN KEY (assigned_by)
        REFERENCES users(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_shift_times CHECK (end_time > start_time),
    CONSTRAINT chk_break_duration CHECK (break_duration_minutes >= 0)
);

-- Shift swap requests table
CREATE TABLE shift_swap_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_shift_id UUID NOT NULL,
    target_shift_id UUID,
    target_staff_id UUID,

    -- Request details
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, CANCELLED

    -- Approval tracking
    approved_by UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_requester_shift FOREIGN KEY (requester_shift_id)
        REFERENCES staff_shifts(id) ON DELETE CASCADE,
    CONSTRAINT fk_target_shift FOREIGN KEY (target_shift_id)
        REFERENCES staff_shifts(id) ON DELETE CASCADE,
    CONSTRAINT fk_target_staff FOREIGN KEY (target_staff_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_approved_by FOREIGN KEY (approved_by)
        REFERENCES users(id) ON DELETE SET NULL
);

-- Recurring shift patterns table (for automated scheduling)
CREATE TABLE recurring_shift_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_profile_id UUID NOT NULL,
    shift_template_id UUID NOT NULL,

    -- Recurrence pattern
    day_of_week day_of_week NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,

    -- Settings
    is_active BOOLEAN DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_recurring_staff FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_template FOREIGN KEY (shift_template_id)
        REFERENCES shift_templates(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_shift_templates_type ON shift_templates(shift_type);
CREATE INDEX idx_shift_templates_active ON shift_templates(is_active);

CREATE INDEX idx_staff_shifts_profile ON staff_shifts(staff_profile_id);
CREATE INDEX idx_staff_shifts_date ON staff_shifts(shift_date);
CREATE INDEX idx_staff_shifts_status ON staff_shifts(status);
CREATE INDEX idx_staff_shifts_template ON staff_shifts(shift_template_id);
CREATE INDEX idx_staff_shifts_date_range ON staff_shifts(shift_date, staff_profile_id);

CREATE INDEX idx_shift_swaps_requester ON shift_swap_requests(requester_shift_id);
CREATE INDEX idx_shift_swaps_target ON shift_swap_requests(target_shift_id);
CREATE INDEX idx_shift_swaps_status ON shift_swap_requests(status);

CREATE INDEX idx_recurring_staff ON recurring_shift_patterns(staff_profile_id);
CREATE INDEX idx_recurring_day ON recurring_shift_patterns(day_of_week);
CREATE INDEX idx_recurring_active ON recurring_shift_patterns(is_active);

-- Comments
COMMENT ON TABLE shift_templates IS 'Reusable shift definitions (e.g., Morning Shift: 8AM-4PM)';
COMMENT ON TABLE staff_shifts IS 'Actual shift assignments for staff members';
COMMENT ON TABLE shift_swap_requests IS 'Requests to swap shifts between staff members';
COMMENT ON TABLE recurring_shift_patterns IS 'Recurring shift patterns for automated scheduling';

COMMENT ON COLUMN staff_shifts.break_duration_minutes IS 'Unpaid break time in minutes';
COMMENT ON COLUMN shift_templates.color_code IS 'Hex color code for calendar display';