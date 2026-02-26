-- V18__create_beauty_services_tables.sql
-- Beauty Services Booking System

-- ========== ENUMS ==========

CREATE TYPE service_status AS ENUM ('ACTIVE', 'INACTIVE', 'DISCONTINUED');

CREATE TYPE booking_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'CANCELLED',
    'RESCHEDULED',
    'IN_PROGRESS',
    'COMPLETED',
    'NO_SHOW'
);

--CREATE TYPE day_of_week AS ENUM (
--    'MONDAY',
--    'TUESDAY',
--    'WEDNESDAY',
--    'THURSDAY',
--    'FRIDAY',
--    'SATURDAY',
--    'SUNDAY'
--);

-- ========== SERVICE CATEGORIES ==========

CREATE TABLE service_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL UNIQUE,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_categories_slug ON service_categories(slug);
CREATE INDEX idx_service_categories_active ON service_categories(is_active);

-- ========== SERVICE SUBCATEGORIES ==========

CREATE TABLE service_subcategories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES service_categories(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(category_id, slug)
);

CREATE INDEX idx_service_subcategories_category ON service_subcategories(category_id);
CREATE INDEX idx_service_subcategories_slug ON service_subcategories(slug);

-- ========== SERVICES ==========

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES service_categories(id),
    subcategory_id UUID REFERENCES service_subcategories(id),
    name VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),

    -- Pricing
    base_price DECIMAL(12, 2) NOT NULL,
    discounted_price DECIMAL(12, 2),

    -- Duration
    duration_minutes INT NOT NULL, -- 30, 60, 90, 120, etc.
    buffer_time_minutes INT DEFAULT 15, -- Time between appointments

    -- Service details
    skill_level VARCHAR(50), -- Beginner, Intermediate, Expert
    max_clients_per_slot INT DEFAULT 1, -- Usually 1 for beauty services

    -- Status & visibility
    status service_status NOT NULL DEFAULT 'ACTIVE',
    is_featured BOOLEAN DEFAULT FALSE,
    requires_consultation BOOLEAN DEFAULT FALSE,

    -- SEO
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_services_category ON services(category_id);
CREATE INDEX idx_services_subcategory ON services(subcategory_id);
CREATE INDEX idx_services_slug ON services(slug);
CREATE INDEX idx_services_status ON services(status);

-- ========== SERVICE IMAGES ==========

CREATE TABLE service_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_images_service ON service_images(service_id);

-- ========== SERVICE ADD-ONS (Optional extras) ==========

CREATE TABLE service_addons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    duration_minutes INT DEFAULT 0, -- Additional time
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_addons_service ON service_addons(service_id);

-- ========== SERVICE STAFF ASSIGNMENT ==========

CREATE TABLE service_staff (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    staff_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE, -- Primary staff for this service
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(service_id, staff_id)
);

CREATE INDEX idx_service_staff_service ON service_staff(service_id);
CREATE INDEX idx_service_staff_staff ON service_staff(staff_id);

-- ========== STAFF AVAILABILITY ==========

CREATE TABLE staff_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week day_of_week NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(staff_id, day_of_week, start_time)
);

CREATE INDEX idx_staff_availability_staff ON staff_availability(staff_id);

-- ========== STAFF BREAKS ==========

CREATE TABLE staff_breaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week day_of_week NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    break_name VARCHAR(100), -- Lunch, Coffee Break, etc.
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_staff_breaks_staff ON staff_breaks(staff_id);

-- ========== BLOCKED DATES (Holidays, Staff Unavailable) ==========

CREATE TABLE blocked_dates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id UUID REFERENCES users(id) ON DELETE CASCADE, -- NULL = whole business closed
    blocked_date DATE NOT NULL,
    start_time TIME, -- NULL = all day
    end_time TIME,
    reason VARCHAR(300),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blocked_dates_staff ON blocked_dates(staff_id);
CREATE INDEX idx_blocked_dates_date ON blocked_dates(blocked_date);

-- ========== BOOKINGS ==========

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_number VARCHAR(50) NOT NULL UNIQUE,

    -- Customer & Service
    customer_id UUID NOT NULL REFERENCES customers(id),
    service_id UUID NOT NULL REFERENCES services(id),
    staff_id UUID NOT NULL REFERENCES users(id), -- Assigned staff

    -- Scheduling
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INT NOT NULL,

    -- Status
    status booking_status NOT NULL DEFAULT 'PENDING',

    -- Pricing
    service_price DECIMAL(12, 2) NOT NULL,
    addons_price DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,

    -- Payment
    payment_status VARCHAR(50) DEFAULT 'PENDING', -- Links to payments table
    paid_at TIMESTAMP,

    -- Notes
    customer_notes TEXT,
    staff_notes TEXT,
    cancellation_reason TEXT,

    -- Timestamps
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_service ON bookings(service_id);
CREATE INDEX idx_bookings_staff ON bookings(staff_id);
CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_number ON bookings(booking_number);

-- ========== BOOKING ADD-ONS ==========

CREATE TABLE booking_addons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    addon_id UUID NOT NULL REFERENCES service_addons(id),
    addon_name VARCHAR(200) NOT NULL, -- Snapshot
    price DECIMAL(12, 2) NOT NULL, -- Snapshot
    duration_minutes INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_addons_booking ON booking_addons(booking_id);

-- ========== BOOKING STATUS HISTORY ==========

CREATE TABLE booking_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    from_status booking_status,
    to_status booking_status NOT NULL,
    changed_by UUID REFERENCES users(id),
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_status_history_booking ON booking_status_history(booking_id);

-- ========== BOOKING REMINDERS ==========

CREATE TABLE booking_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    reminder_type VARCHAR(50) NOT NULL, -- SMS, EMAIL, PUSH
    scheduled_for TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_reminders_booking ON booking_reminders(booking_id);
CREATE INDEX idx_booking_reminders_scheduled ON booking_reminders(scheduled_for);

-- ========== SEQUENCES ==========

CREATE SEQUENCE booking_number_seq START WITH 1 INCREMENT BY 1;

-- ========== COMMENTS ==========

COMMENT ON TABLE service_categories IS 'Main categories for beauty services (Hair, Nails, Facial, etc.)';
COMMENT ON TABLE services IS 'Individual beauty services with pricing and duration';
COMMENT ON TABLE bookings IS 'Customer service bookings/appointments';
COMMENT ON TABLE staff_availability IS 'Regular weekly availability for staff members';
COMMENT ON TABLE blocked_dates IS 'Holidays and unavailable dates';

COMMENT ON COLUMN services.duration_minutes IS 'Base duration of service in minutes';
COMMENT ON COLUMN services.buffer_time_minutes IS 'Buffer time between appointments for cleanup/setup';
COMMENT ON COLUMN bookings.booking_number IS 'Unique booking reference (BK-2026-00001)';