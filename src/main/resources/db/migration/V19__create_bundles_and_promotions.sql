-- V19__create_bundles_and_promotions.sql
-- Product Bundles & Promotions System

-- ========== ENUMS ==========

CREATE TYPE bundle_status AS ENUM ('ACTIVE', 'INACTIVE', 'SCHEDULED', 'EXPIRED');

CREATE TYPE promotion_type AS ENUM (
    'PERCENTAGE_DISCOUNT',
    'FIXED_AMOUNT_DISCOUNT',
    'BUY_X_GET_Y',
    'FREE_SHIPPING',
    'BUNDLE_DISCOUNT'
);

CREATE TYPE promotion_status AS ENUM ('ACTIVE', 'INACTIVE', 'SCHEDULED', 'EXPIRED');

-- ========== PRODUCT BUNDLES ==========

CREATE TABLE product_bundles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),

    -- Pricing
    original_total_price DECIMAL(12, 2) NOT NULL, -- Sum of individual prices
    bundle_price DECIMAL(12, 2) NOT NULL, -- Discounted bundle price
    savings_amount DECIMAL(12, 2) NOT NULL, -- How much customer saves
    savings_percentage DECIMAL(5, 2), -- Percentage saved

    -- Bundle details
    min_quantity INT DEFAULT 1,
    max_quantity INT DEFAULT 100,
    available_stock INT, -- NULL = unlimited

    -- Visibility & status
    status bundle_status NOT NULL DEFAULT 'ACTIVE',
    is_featured BOOLEAN DEFAULT FALSE,

    -- Scheduling
    start_date TIMESTAMP,
    end_date TIMESTAMP,

    -- Images
    primary_image_url VARCHAR(500),

    -- SEO
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

CREATE INDEX idx_bundles_slug ON product_bundles(slug);
CREATE INDEX idx_bundles_status ON product_bundles(status);
CREATE INDEX idx_bundles_featured ON product_bundles(is_featured);
CREATE INDEX idx_bundles_dates ON product_bundles(start_date, end_date);

-- ========== BUNDLE PRODUCTS (Items in a bundle) ==========

CREATE TABLE bundle_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_id UUID NOT NULL REFERENCES product_bundles(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INT NOT NULL DEFAULT 1,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(bundle_id, product_id)
);

CREATE INDEX idx_bundle_products_bundle ON bundle_products(bundle_id);
CREATE INDEX idx_bundle_products_product ON bundle_products(product_id);

-- ========== BUNDLE IMAGES ==========

CREATE TABLE bundle_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_id UUID NOT NULL REFERENCES product_bundles(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bundle_images_bundle ON bundle_images(bundle_id);

-- ========== PROMOTIONS ==========

CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(300) NOT NULL,
    code VARCHAR(50) UNIQUE, -- Promo code (optional, NULL = auto-applied)
    description TEXT,

    -- Promotion type and value
    promotion_type promotion_type NOT NULL,
    discount_value DECIMAL(12, 2), -- Percentage or fixed amount

    -- Conditions
    min_purchase_amount DECIMAL(12, 2), -- Minimum order value
    max_discount_amount DECIMAL(12, 2), -- Cap on discount amount
    usage_limit INT, -- Total number of times promo can be used
    usage_per_customer INT DEFAULT 1, -- Times each customer can use

    -- Applicability
    applies_to VARCHAR(50) NOT NULL, -- ALL, CATEGORIES, PRODUCTS, SERVICES

    -- Status
    status promotion_status NOT NULL DEFAULT 'ACTIVE',

    -- Scheduling
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,

    -- Tracking
    total_usage_count INT DEFAULT 0,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

CREATE INDEX idx_promotions_code ON promotions(code) WHERE code IS NOT NULL;
CREATE INDEX idx_promotions_status ON promotions(status);
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_type ON promotions(promotion_type);

-- ========== PROMOTION PRODUCTS (Products eligible for promotion) ==========

CREATE TABLE promotion_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(promotion_id, product_id)
);

CREATE INDEX idx_promotion_products_promotion ON promotion_products(promotion_id);
CREATE INDEX idx_promotion_products_product ON promotion_products(product_id);

-- ========== PROMOTION CATEGORIES (Categories eligible for promotion) ==========

CREATE TABLE promotion_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(promotion_id, category_id)
);

CREATE INDEX idx_promotion_categories_promotion ON promotion_categories(promotion_id);
CREATE INDEX idx_promotion_categories_category ON promotion_categories(category_id);

-- ========== PROMOTION SERVICES (Services eligible for promotion) ==========

CREATE TABLE promotion_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(promotion_id, service_id)
);

CREATE INDEX idx_promotion_services_promotion ON promotion_services(promotion_id);
CREATE INDEX idx_promotion_services_service ON promotion_services(service_id);

-- ========== PROMOTION USAGE TRACKING ==========

CREATE TABLE promotion_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    order_id UUID REFERENCES orders(id),
    discount_amount DECIMAL(12, 2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_promotion_usage_promotion ON promotion_usage(promotion_id);
CREATE INDEX idx_promotion_usage_customer ON promotion_usage(customer_id);
CREATE INDEX idx_promotion_usage_order ON promotion_usage(order_id);

-- ========== COMMENTS ==========

COMMENT ON TABLE product_bundles IS 'Product bundles with discounted pricing';
COMMENT ON TABLE promotions IS 'Promotional campaigns and discount codes';
COMMENT ON TABLE promotion_usage IS 'Tracks promotion usage per customer';

COMMENT ON COLUMN product_bundles.original_total_price IS 'Sum of individual product prices';
COMMENT ON COLUMN product_bundles.bundle_price IS 'Discounted bundle price';
COMMENT ON COLUMN product_bundles.savings_amount IS 'Amount saved by buying bundle';
COMMENT ON COLUMN promotions.code IS 'Promo code - NULL means auto-applied';
COMMENT ON COLUMN promotions.applies_to IS 'ALL, CATEGORIES, PRODUCTS, SERVICES';