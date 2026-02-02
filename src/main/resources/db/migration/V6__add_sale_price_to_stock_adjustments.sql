-- V6__add_sale_price_to_stock_adjustments.sql
-- Add sale_price column to track actual selling prices

ALTER TABLE stock_adjustments
ADD COLUMN sale_price DECIMAL(12, 2);

-- Add comment explaining the column
COMMENT ON COLUMN stock_adjustments.sale_price IS 'Actual price per unit for sales (allows tracking bargained prices)';