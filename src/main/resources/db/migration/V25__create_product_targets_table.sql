CREATE TABLE product_targets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    target_year INT NOT NULL,
    target_month INT NOT NULL,
    target_units INT NOT NULL CHECK (target_units > 0),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(product_id, target_year, target_month)
);
CREATE INDEX idx_product_targets_product_id ON product_targets(product_id);
CREATE INDEX idx_product_targets_year_month ON product_targets(target_year, target_month);
