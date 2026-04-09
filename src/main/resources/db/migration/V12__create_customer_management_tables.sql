-- V12__create_customer_management_tables.sql
-- Customer Management System (E-commerce)

-- Create enum for customer type
CREATE TYPE customer_type AS ENUM ('GUEST', 'REGISTERED');

-- Create enum for auth provider
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE', 'FACEBOOK', 'APPLE');

-- Create enum for customer status
CREATE TYPE customer_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');

-- Customers table (supports both guest and registered)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Customer type
    customer_type customer_type NOT NULL DEFAULT 'REGISTERED',

    -- Basic info
    email VARCHAR(255),
    phone VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),

    -- Authentication (null for guests)
    password_hash VARCHAR(255),
    auth_provider auth_provider DEFAULT 'LOCAL',
    google_id VARCHAR(255),
    facebook_id VARCHAR(255),
    apple_id VARCHAR(255),

    -- Profile
    date_of_birth DATE,
    gender VARCHAR(20),
    profile_image_url VARCHAR(500),

    -- Status
    status customer_status NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT false,
    phone_verified BOOLEAN DEFAULT false,

    -- Preferences
    newsletter_subscribed BOOLEAN DEFAULT false,
    sms_notifications BOOLEAN DEFAULT false,

    -- Guest session (for guest checkout)
    guest_session_id VARCHAR(100),
    guest_session_expires_at TIMESTAMP,

    -- Audit fields
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_registered_email CHECK (
        customer_type = 'GUEST' OR email IS NOT NULL
    ),
    CONSTRAINT chk_local_auth_password CHECK (
        auth_provider != 'LOCAL' OR password_hash IS NOT NULL
    ),
    CONSTRAINT uq_email UNIQUE (email),
    CONSTRAINT uq_google_id UNIQUE (google_id),
    CONSTRAINT uq_guest_session UNIQUE (guest_session_id)
);

-- Customer addresses table
CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,

    -- Address details
    address_label VARCHAR(50), -- e.g., "Home", "Office", "Billing"
    recipient_name VARCHAR(200),
    phone VARCHAR(20),

    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL DEFAULT 'Nigeria',

    -- Location (for delivery optimization)
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),

    -- Flags
    is_default BOOLEAN DEFAULT false,
    is_billing_address BOOLEAN DEFAULT false,
    is_shipping_address BOOLEAN DEFAULT true,

    -- Delivery instructions
    delivery_instructions TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE
);

-- Wishlist table
CREATE TABLE wishlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    product_id UUID NOT NULL,

    -- Priority/notes
    priority INTEGER DEFAULT 0,
    notes TEXT,

    -- Price tracking
    price_when_added DECIMAL(12, 2),
    notify_on_price_drop BOOLEAN DEFAULT false,
    target_price DECIMAL(12, 2),

    -- Audit fields
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_wishlist_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,

    -- Prevent duplicates
    CONSTRAINT uq_customer_product UNIQUE (customer_id, product_id)
);

-- Customer analytics table (foundation for spending tracking)
CREATE TABLE customer_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE,

    -- Spending metrics
    total_orders INTEGER DEFAULT 0,
    total_spent DECIMAL(12, 2) DEFAULT 0,
    average_order_value DECIMAL(12, 2) DEFAULT 0,

    -- Budget management (foundation)
    monthly_budget_cap DECIMAL(12, 2),
    current_month_spent DECIMAL(12, 2) DEFAULT 0,
    budget_alert_threshold DECIMAL(5, 2) DEFAULT 80.0, -- Alert at 80%

    -- Lifetime value
    lifetime_value DECIMAL(12, 2) DEFAULT 0,

    -- Engagement
    last_purchase_at TIMESTAMP,
    first_purchase_at TIMESTAMP,
    days_since_last_purchase INTEGER,

    -- Product preferences (for future recommendations)
    favorite_category_id UUID,
    favorite_brand VARCHAR(100),

    -- Audit fields
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_analytics_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_analytics_category FOREIGN KEY (favorite_category_id)
        REFERENCES categories(id) ON DELETE SET NULL
);

-- Customer spending history (monthly snapshots)
CREATE TABLE customer_spending_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,

    -- Period
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,

    -- Metrics
    orders_count INTEGER DEFAULT 0,
    total_spent DECIMAL(12, 2) DEFAULT 0,
    average_order_value DECIMAL(12, 2) DEFAULT 0,

    -- Budget tracking
    budget_cap DECIMAL(12, 2),
    budget_utilized_percentage DECIMAL(5, 2),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_spending_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,

    -- Unique per customer per month
    CONSTRAINT uq_customer_period UNIQUE (customer_id, year, month)
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reset_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE
);

-- Email verification tokens
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_verification_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_type ON customers(customer_type);
CREATE INDEX idx_customer_status ON customers(status);
CREATE INDEX idx_customer_google ON customers(google_id);
CREATE INDEX idx_customer_guest_session ON customers(guest_session_id);

CREATE INDEX idx_address_customer ON customer_addresses(customer_id);
CREATE INDEX idx_address_default ON customer_addresses(customer_id, is_default);

CREATE INDEX idx_wishlist_customer ON wishlist_items(customer_id);
CREATE INDEX idx_wishlist_product ON wishlist_items(product_id);

CREATE INDEX idx_analytics_customer ON customer_analytics(customer_id);

CREATE INDEX idx_spending_customer ON customer_spending_history(customer_id);
CREATE INDEX idx_spending_period ON customer_spending_history(year, month);

-- Comments
COMMENT ON TABLE customers IS 'Customer accounts - supports both guest and registered users';
COMMENT ON TABLE customer_addresses IS 'Customer shipping and billing addresses';
COMMENT ON TABLE wishlist_items IS 'Customer product wishlist with price tracking';
COMMENT ON TABLE customer_analytics IS 'Customer spending analytics and budget management';
COMMENT ON TABLE customer_spending_history IS 'Monthly spending snapshots for trend analysis';

COMMENT ON COLUMN customers.customer_type IS 'GUEST for one-time checkout, REGISTERED for account holders';
COMMENT ON COLUMN customers.guest_session_id IS 'Temporary session ID for guest checkout tracking';
COMMENT ON COLUMN customer_analytics.monthly_budget_cap IS 'Optional spending limit per month';
COMMENT ON COLUMN customer_analytics.budget_alert_threshold IS 'Percentage threshold for budget alerts (default 80%)';