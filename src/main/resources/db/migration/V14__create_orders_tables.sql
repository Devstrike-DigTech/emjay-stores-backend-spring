-- V14__create_orders_tables.sql
-- Orders & Checkout System

-- Create enum for order status
CREATE TYPE order_status AS ENUM (
    'PENDING_PAYMENT',
    'PAYMENT_FAILED',
    'PAID',
    'PROCESSING',
    'SHIPPED',
    'DELIVERED',
    'CANCELLED',
    'REFUNDED'
);

-- Create enum for payment status
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'AUTHORIZED',
    'CAPTURED',
    'FAILED',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
);

-- Create enum for payment method
CREATE TYPE payment_method AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'BANK_TRANSFER',
    'PAYSTACK',
    'FLUTTERWAVE',
    'CASH_ON_DELIVERY'
);

-- Create enum for shipment status
CREATE TYPE shipment_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'SHIPPED',
    'IN_TRANSIT',
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'FAILED_DELIVERY',
    'RETURNED'
);

-- Orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Order identification
    order_number VARCHAR(50) NOT NULL UNIQUE,

    -- Customer reference
    customer_id UUID NOT NULL,

    -- Order details
    status order_status NOT NULL DEFAULT 'PENDING_PAYMENT',

    -- Pricing
    subtotal DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12, 2) NOT NULL,
    shipping_cost DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,

    -- Applied coupon
    coupon_code VARCHAR(50),

    -- Shipping address (snapshot)
    shipping_address_id UUID,
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    recipient_name VARCHAR(200),
    recipient_phone VARCHAR(20),

    -- Billing address (snapshot)
    billing_address_id UUID,
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(100),

    -- Customer notes
    customer_notes TEXT,

    -- Internal notes
    admin_notes TEXT,

    -- Important dates
    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_order_shipping_address FOREIGN KEY (shipping_address_id)
        REFERENCES customer_addresses(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_billing_address FOREIGN KEY (billing_address_id)
        REFERENCES customer_addresses(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_order_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_order_tax CHECK (tax_amount >= 0),
    CONSTRAINT chk_order_shipping CHECK (shipping_cost >= 0),
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

-- Order items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,

    -- Quantity
    quantity INTEGER NOT NULL,

    -- Pricing (snapshot at time of order)
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,

    -- Product details (snapshot)
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    product_image_url VARCHAR(500),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,

    -- Constraints
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_item_subtotal CHECK (subtotal >= 0)
);

-- Payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,

    -- Payment details
    payment_method payment_method NOT NULL,
    payment_status payment_status NOT NULL DEFAULT 'PENDING',

    -- Amount
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',

    -- Payment gateway
    gateway_provider VARCHAR(50),
    gateway_transaction_id VARCHAR(255),
    gateway_reference VARCHAR(255),

    -- Payment metadata (JSON)
    metadata TEXT,

    -- Important dates
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    failed_at TIMESTAMP,

    -- Error handling
    failure_reason TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_payment_amount CHECK (amount >= 0)
);

-- Shipments table
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,

    -- Shipment details
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    status shipment_status NOT NULL DEFAULT 'PENDING',

    -- Shipping method
    shipping_method VARCHAR(100),
    estimated_delivery_date DATE,
    actual_delivery_date DATE,

    -- Location tracking
    current_location VARCHAR(255),

    -- Notes
    notes TEXT,

    -- Important dates
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);

-- Shipment tracking events table
CREATE TABLE shipment_tracking_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL,

    -- Event details
    status shipment_status NOT NULL,
    location VARCHAR(255),
    description TEXT,
    event_timestamp TIMESTAMP NOT NULL,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_tracking_shipment FOREIGN KEY (shipment_id)
        REFERENCES shipments(id) ON DELETE CASCADE
);

-- Order status history table
CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,

    -- Status change
    from_status order_status,
    to_status order_status NOT NULL,

    -- Change details
    changed_by UUID,
    reason TEXT,

    -- Audit fields
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_status_history_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_status_changed_by FOREIGN KEY (changed_by)
        REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_ordered_at ON orders(ordered_at);
CREATE INDEX idx_order_paid_at ON orders(paid_at);

CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_product ON order_items(product_id);

CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(payment_status);
CREATE INDEX idx_payment_gateway_ref ON payments(gateway_reference);

CREATE INDEX idx_shipment_order ON shipments(order_id);
CREATE INDEX idx_shipment_tracking ON shipments(tracking_number);
CREATE INDEX idx_shipment_status ON shipments(status);

CREATE INDEX idx_tracking_event_shipment ON shipment_tracking_events(shipment_id);

CREATE INDEX idx_status_history_order ON order_status_history(order_id);

-- Create sequence for order numbers
CREATE SEQUENCE order_number_seq START WITH 1;

-- Comments
COMMENT ON TABLE orders IS 'Customer orders with complete transaction details';
COMMENT ON TABLE order_items IS 'Individual products in orders (price snapshot)';
COMMENT ON TABLE payments IS 'Payment transactions linked to orders';
COMMENT ON TABLE shipments IS 'Shipment and delivery tracking';
COMMENT ON TABLE shipment_tracking_events IS 'Detailed shipment tracking history';
COMMENT ON TABLE order_status_history IS 'Audit trail of order status changes';

COMMENT ON COLUMN orders.order_number IS 'Unique human-readable order identifier (e.g., ORD-2026-00001)';
COMMENT ON COLUMN orders.shipping_address_line1 IS 'Address snapshot at time of order';
COMMENT ON COLUMN payments.gateway_transaction_id IS 'Payment gateway transaction reference';
COMMENT ON COLUMN shipments.tracking_number IS 'Courier tracking number';