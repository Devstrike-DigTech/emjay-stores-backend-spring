-- V13__create_shopping_cart_tables.sql
-- Shopping Cart System

-- Create enum for cart status
CREATE TYPE cart_status AS ENUM ('ACTIVE', 'ABANDONED', 'CONVERTED', 'MERGED');

-- Shopping carts table (supports both guest and registered customers)
CREATE TABLE shopping_carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Customer reference (null for guest)
    customer_id UUID,

    -- Guest session (null for registered)
    guest_session_id VARCHAR(100),

    -- Cart metadata
    status cart_status NOT NULL DEFAULT 'ACTIVE',

    -- Pricing
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,

    -- Applied coupon
    coupon_code VARCHAR(50),

    -- Expiry (for guest carts)
    expires_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_cart_owner CHECK (
        (customer_id IS NOT NULL AND guest_session_id IS NULL) OR
        (customer_id IS NULL AND guest_session_id IS NOT NULL)
    ),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_tax CHECK (tax_amount >= 0),
    CONSTRAINT chk_total CHECK (total_amount >= 0),

    -- Unique constraints
    CONSTRAINT uq_customer_active_cart UNIQUE (customer_id, status),
    CONSTRAINT uq_guest_session_cart UNIQUE (guest_session_id)
);

-- Cart items table
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,

    -- Quantity
    quantity INTEGER NOT NULL,

    -- Pricing (snapshot at time of add)
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,

    -- Product details (snapshot for historical accuracy)
    product_name VARCHAR(255),
    product_sku VARCHAR(100),
    product_image_url VARCHAR(500),

    -- Audit fields
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id)
        REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_item_subtotal CHECK (subtotal >= 0),

    -- Unique: one product per cart
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)
);

-- Abandoned cart tracking (for marketing/recovery)
CREATE TABLE abandoned_carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    customer_id UUID,

    -- Abandonment details
    abandoned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cart_value DECIMAL(12, 2) NOT NULL,
    items_count INTEGER NOT NULL,

    -- Recovery tracking
    recovery_email_sent BOOLEAN DEFAULT false,
    recovery_email_sent_at TIMESTAMP,
    recovered BOOLEAN DEFAULT false,
    recovered_at TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_abandoned_cart FOREIGN KEY (cart_id)
        REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_abandoned_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_cart_customer ON shopping_carts(customer_id);
CREATE INDEX idx_cart_guest_session ON shopping_carts(guest_session_id);
CREATE INDEX idx_cart_status ON shopping_carts(status);
CREATE INDEX idx_cart_expires ON shopping_carts(expires_at);

CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_product ON cart_items(product_id);

CREATE INDEX idx_abandoned_customer ON abandoned_carts(customer_id);
CREATE INDEX idx_abandoned_recovered ON abandoned_carts(recovered);

-- Comments
COMMENT ON TABLE shopping_carts IS 'Shopping carts for both guest and registered customers';
COMMENT ON TABLE cart_items IS 'Individual items in shopping carts';
COMMENT ON TABLE abandoned_carts IS 'Tracking for cart abandonment and recovery campaigns';

COMMENT ON COLUMN shopping_carts.customer_id IS 'For registered customers - mutually exclusive with guest_session_id';
COMMENT ON COLUMN shopping_carts.guest_session_id IS 'For guest checkout - mutually exclusive with customer_id';
COMMENT ON COLUMN shopping_carts.expires_at IS 'Expiry time for guest carts (24 hours)';
COMMENT ON COLUMN cart_items.product_name IS 'Snapshot of product name at time of adding to cart';
COMMENT ON COLUMN cart_items.unit_price IS 'Price snapshot at time of adding (may differ from current price)';