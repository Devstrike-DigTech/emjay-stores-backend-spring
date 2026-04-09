CREATE TYPE ad_target AS ENUM ('ALL', 'PRODUCTS', 'SERVICES', 'CATEGORIES');
CREATE TYPE ad_status AS ENUM ('ACTIVE', 'INACTIVE', 'SCHEDULED', 'EXPIRED');

CREATE TABLE ads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    headline VARCHAR(300) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    applies_to ad_target NOT NULL DEFAULT 'ALL',
    status ad_status NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ad_targets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ad_id UUID NOT NULL REFERENCES ads(id) ON DELETE CASCADE,
    target_id UUID NOT NULL
);

CREATE INDEX idx_ads_status ON ads(status);
CREATE INDEX idx_ads_start_end ON ads(start_date, end_date);
