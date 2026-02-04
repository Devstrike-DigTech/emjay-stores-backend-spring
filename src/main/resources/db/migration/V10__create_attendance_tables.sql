-- V10__create_attendance_tables.sql
-- Attendance Tracking System (Clock-In/Out)

-- Create enum for attendance status
CREATE TYPE attendance_status AS ENUM ('PRESENT', 'LATE', 'EARLY_DEPARTURE', 'ABSENT', 'ON_BREAK');

-- Attendance records table (clock-in/out events)
CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_profile_id UUID NOT NULL,
    staff_shift_id UUID,

    -- Clock-in details
    clock_in_time TIMESTAMP NOT NULL,
    clock_in_location VARCHAR(255),
    clock_in_latitude DECIMAL(10, 8),
    clock_in_longitude DECIMAL(11, 8),
    clock_in_notes TEXT,

    -- Clock-out details
    clock_out_time TIMESTAMP,
    clock_out_location VARCHAR(255),
    clock_out_latitude DECIMAL(10, 8),
    clock_out_longitude DECIMAL(11, 8),
    clock_out_notes TEXT,

    -- Break tracking
    total_break_minutes INTEGER DEFAULT 0,

    -- Calculated fields
    scheduled_start_time TIMESTAMP,
    scheduled_end_time TIMESTAMP,
    actual_work_minutes INTEGER,
    is_late BOOLEAN DEFAULT false,
    late_minutes INTEGER DEFAULT 0,
    is_early_departure BOOLEAN DEFAULT false,
    early_departure_minutes INTEGER DEFAULT 0,

    -- Status
    status attendance_status NOT NULL DEFAULT 'PRESENT',

    -- Additional tracking
    approved_by UUID,
    approval_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_attendance_staff FOREIGN KEY (staff_profile_id)
        REFERENCES staff_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_shift FOREIGN KEY (staff_shift_id)
        REFERENCES staff_shifts(id) ON DELETE SET NULL,
    CONSTRAINT fk_approved_by FOREIGN KEY (approved_by)
        REFERENCES users(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_clock_times CHECK (clock_out_time IS NULL OR clock_out_time > clock_in_time),
    CONSTRAINT chk_break_minutes CHECK (total_break_minutes >= 0),
    CONSTRAINT chk_late_minutes CHECK (late_minutes >= 0),
    CONSTRAINT chk_early_minutes CHECK (early_departure_minutes >= 0)
);

-- Break records table (track individual break periods)
CREATE TABLE break_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_record_id UUID NOT NULL,

    -- Break details
    break_start_time TIMESTAMP NOT NULL,
    break_end_time TIMESTAMP,
    break_duration_minutes INTEGER,
    break_type VARCHAR(50) DEFAULT 'REGULAR', -- REGULAR, LUNCH, EMERGENCY
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_break_attendance FOREIGN KEY (attendance_record_id)
        REFERENCES attendance_records(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_break_times CHECK (break_end_time IS NULL OR break_end_time > break_start_time)
);

-- Attendance adjustments table (manual corrections)
CREATE TABLE attendance_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_record_id UUID NOT NULL,

    -- Adjustment details
    adjustment_type VARCHAR(50) NOT NULL, -- TIME_CORRECTION, STATUS_CHANGE, MANUAL_ENTRY
    original_clock_in TIMESTAMP,
    adjusted_clock_in TIMESTAMP,
    original_clock_out TIMESTAMP,
    adjusted_clock_out TIMESTAMP,
    reason TEXT NOT NULL,

    -- Approval
    requested_by UUID NOT NULL,
    approved_by UUID,
    approved_at TIMESTAMP,
    approval_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_adjustment_attendance FOREIGN KEY (attendance_record_id)
        REFERENCES attendance_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_requested_by FOREIGN KEY (requested_by)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_adjustment_approved_by FOREIGN KEY (approved_by)
        REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_attendance_staff ON attendance_records(staff_profile_id);
CREATE INDEX idx_attendance_shift ON attendance_records(staff_shift_id);
CREATE INDEX idx_attendance_clock_in ON attendance_records(clock_in_time);
CREATE INDEX idx_attendance_clock_out ON attendance_records(clock_out_time);
CREATE INDEX idx_attendance_status ON attendance_records(status);
CREATE INDEX idx_attendance_date ON attendance_records(DATE(clock_in_time));

CREATE INDEX idx_break_attendance ON break_records(attendance_record_id);
CREATE INDEX idx_break_start ON break_records(break_start_time);

CREATE INDEX idx_adjustment_attendance ON attendance_adjustments(attendance_record_id);
CREATE INDEX idx_adjustment_status ON attendance_adjustments(approval_status);

-- Comments
COMMENT ON TABLE attendance_records IS 'Clock-in/out records for staff attendance tracking';
COMMENT ON TABLE break_records IS 'Individual break periods during work shifts';
COMMENT ON TABLE attendance_adjustments IS 'Manual corrections to attendance records';

COMMENT ON COLUMN attendance_records.actual_work_minutes IS 'Total minutes worked excluding breaks';
COMMENT ON COLUMN attendance_records.is_late IS 'True if clocked in after scheduled start time';
COMMENT ON COLUMN attendance_records.late_minutes IS 'Minutes late from scheduled start';
COMMENT ON COLUMN attendance_records.is_early_departure IS 'True if clocked out before scheduled end';
COMMENT ON COLUMN attendance_records.early_departure_minutes IS 'Minutes early from scheduled end';