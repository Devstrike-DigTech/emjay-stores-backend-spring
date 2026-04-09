-- V22__create_analytics_system.sql
-- Analytics & Business Intelligence System

-- ========== ENUMS ==========

CREATE TYPE report_type AS ENUM (
    'SALES_SUMMARY',
    'REVENUE_ANALYSIS',
    'PRODUCT_PERFORMANCE',
    'CUSTOMER_INSIGHTS',
    'BOOKING_ANALYTICS',
    'INVENTORY_STATUS',
    'STAFF_PERFORMANCE',
    'CUSTOM'
);

CREATE TYPE report_period AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM');

CREATE TYPE metric_type AS ENUM (
    'REVENUE',
    'ORDERS',
    'BOOKINGS',
    'CUSTOMERS',
    'PRODUCTS_SOLD',
    'AVERAGE_ORDER_VALUE',
    'CONVERSION_RATE',
    'CUSTOMER_LIFETIME_VALUE'
);

-- ========== SALES ANALYTICS ==========

CREATE TABLE sales_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Time period
    period_date DATE NOT NULL,
    period_type report_period NOT NULL,

    -- Sales metrics
    total_revenue DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    total_items_sold INT NOT NULL DEFAULT 0,
    average_order_value DECIMAL(15, 2) NOT NULL DEFAULT 0,

    -- Order status breakdown
    pending_orders INT NOT NULL DEFAULT 0,
    completed_orders INT NOT NULL DEFAULT 0,
    cancelled_orders INT NOT NULL DEFAULT 0,

    -- Payment breakdown
    cash_payments DECIMAL(15, 2) NOT NULL DEFAULT 0,
    card_payments DECIMAL(15, 2) NOT NULL DEFAULT 0,
    transfer_payments DECIMAL(15, 2) NOT NULL DEFAULT 0,

    -- Customer metrics
    new_customers INT NOT NULL DEFAULT 0,
    returning_customers INT NOT NULL DEFAULT 0,
    unique_customers INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(period_date, period_type)
);

CREATE INDEX idx_sales_analytics_period ON sales_analytics(period_date, period_type);
CREATE INDEX idx_sales_analytics_date ON sales_analytics(period_date DESC);

-- ========== PRODUCT PERFORMANCE ==========

CREATE TABLE product_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,

    -- Time period
    period_date DATE NOT NULL,
    period_type report_period NOT NULL,

    -- Sales metrics
    units_sold INT NOT NULL DEFAULT 0,
    revenue DECIMAL(15, 2) NOT NULL DEFAULT 0,
    orders_count INT NOT NULL DEFAULT 0,

    -- Performance indicators
    average_unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    revenue_rank INT,
    units_rank INT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(product_id, period_date, period_type)
);

CREATE INDEX idx_product_performance_product ON product_performance(product_id);
CREATE INDEX idx_product_performance_period ON product_performance(period_date, period_type);
CREATE INDEX idx_product_performance_revenue ON product_performance(revenue DESC);

-- ========== CUSTOMER ANALYTICS ==========

CREATE TABLE admin_customer_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,

    -- Lifetime metrics
    total_orders INT NOT NULL DEFAULT 0,
    total_bookings INT NOT NULL DEFAULT 0,
    total_spent DECIMAL(15, 2) NOT NULL DEFAULT 0,
    average_order_value DECIMAL(15, 2) NOT NULL DEFAULT 0,

    -- Engagement metrics
    first_purchase_date DATE,
    last_purchase_date DATE,
    days_since_last_purchase INT,
    purchase_frequency DECIMAL(10, 2), -- Orders per month

    -- Customer segmentation
    customer_segment VARCHAR(50), -- VIP, REGULAR, OCCASIONAL, AT_RISK
    lifetime_value_tier VARCHAR(20), -- HIGH, MEDIUM, LOW

    -- Last updated
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(customer_id)
);

CREATE INDEX idx_admin_customer_analytics_customer ON admin_customer_analytics(customer_id);
CREATE INDEX idx_admin_customer_analytics_segment ON admin_customer_analytics(customer_segment);
CREATE INDEX idx_admin_customer_analytics_value ON admin_customer_analytics(total_spent DESC);

-- ========== BOOKING ANALYTICS ==========

CREATE TABLE booking_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Time period
    period_date DATE NOT NULL,
    period_type report_period NOT NULL,

    -- Booking metrics
    total_bookings INT NOT NULL DEFAULT 0,
    completed_bookings INT NOT NULL DEFAULT 0,
    cancelled_bookings INT NOT NULL DEFAULT 0,
    no_show_bookings INT NOT NULL DEFAULT 0,

    -- Revenue metrics
    total_revenue DECIMAL(15, 2) NOT NULL DEFAULT 0,
    average_booking_value DECIMAL(15, 2) NOT NULL DEFAULT 0,

    -- Service metrics
    most_popular_service_id UUID,
    most_popular_service_name VARCHAR(200),
    most_popular_service_bookings INT DEFAULT 0,

    -- Staff metrics
    most_productive_staff_id UUID,
    most_productive_staff_bookings INT DEFAULT 0,

    -- Efficiency
    occupancy_rate DECIMAL(5, 2), -- Percentage of available slots booked
    cancellation_rate DECIMAL(5, 2),
    no_show_rate DECIMAL(5, 2),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(period_date, period_type)
);

CREATE INDEX idx_booking_analytics_period ON booking_analytics(period_date, period_type);

-- ========== INVENTORY ANALYTICS ==========

CREATE TABLE inventory_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Snapshot date
    snapshot_date DATE NOT NULL,

    -- Inventory metrics
    total_products INT NOT NULL DEFAULT 0,
    total_stock_value DECIMAL(15, 2) NOT NULL DEFAULT 0,
    low_stock_products INT NOT NULL DEFAULT 0,
    out_of_stock_products INT NOT NULL DEFAULT 0,

    -- Stock movement
    products_added INT NOT NULL DEFAULT 0,
    products_sold INT NOT NULL DEFAULT 0,
    stock_adjustments INT NOT NULL DEFAULT 0,

    -- Top movers
    fastest_moving_product_id UUID,
    fastest_moving_product_name VARCHAR(200),
    fastest_moving_units INT DEFAULT 0,

    slowest_moving_product_id UUID,
    slowest_moving_product_name VARCHAR(200),
    slowest_moving_days_in_stock INT DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(snapshot_date)
);

CREATE INDEX idx_inventory_analytics_date ON inventory_analytics(snapshot_date DESC);

-- ========== STAFF PERFORMANCE ==========

CREATE TABLE staff_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id UUID NOT NULL,

    -- Time period
    period_date DATE NOT NULL,
    period_type report_period NOT NULL,

    -- Booking metrics
    total_bookings INT NOT NULL DEFAULT 0,
    completed_bookings INT NOT NULL DEFAULT 0,
    cancelled_bookings INT NOT NULL DEFAULT 0,

    -- Revenue metrics
    total_revenue DECIMAL(15, 2) NOT NULL DEFAULT 0,
    average_booking_value DECIMAL(15, 2) NOT NULL DEFAULT 0,

    -- Performance indicators
    completion_rate DECIMAL(5, 2),
    customer_satisfaction_score DECIMAL(3, 2), -- Future: from reviews
    revenue_rank INT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(staff_id, period_date, period_type)
);

CREATE INDEX idx_staff_performance_staff ON staff_performance(staff_id);
CREATE INDEX idx_staff_performance_period ON staff_performance(period_date, period_type);
CREATE INDEX idx_staff_performance_revenue ON staff_performance(total_revenue DESC);

-- ========== DASHBOARD METRICS (Real-time) ==========

CREATE TABLE dashboard_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15, 2) NOT NULL,
    metric_type metric_type NOT NULL,

    -- Comparison
    previous_value DECIMAL(15, 2),
    change_percentage DECIMAL(5, 2),

    -- Context
    period_label VARCHAR(50), -- "Today", "This Week", "This Month"
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(metric_name, period_label)
);

CREATE INDEX idx_dashboard_metrics_type ON dashboard_metrics(metric_type);
CREATE INDEX idx_dashboard_metrics_calculated ON dashboard_metrics(calculated_at DESC);

-- ========== COMMENTS ==========

COMMENT ON TABLE sales_analytics IS 'Aggregated sales data by time period';
COMMENT ON TABLE product_performance IS 'Product sales performance tracking';
COMMENT ON TABLE admin_customer_analytics IS 'Customer lifetime value and behavior';
COMMENT ON TABLE booking_analytics IS 'Service booking analytics';
COMMENT ON TABLE inventory_analytics IS 'Inventory status and movement';
COMMENT ON TABLE staff_performance IS 'Staff booking performance metrics';
COMMENT ON TABLE dashboard_metrics IS 'Real-time dashboard KPIs';

COMMENT ON COLUMN admin_customer_analytics.customer_segment IS 'VIP, REGULAR, OCCASIONAL, AT_RISK';
COMMENT ON COLUMN admin_customer_analytics.lifetime_value_tier IS 'HIGH, MEDIUM, LOW based on total_spent';
COMMENT ON COLUMN booking_analytics.occupancy_rate IS 'Percentage of available booking slots that were booked';