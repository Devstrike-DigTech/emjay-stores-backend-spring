CREATE TABLE store_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_name VARCHAR(200) NOT NULL DEFAULT 'Emjay Stores',
    store_description TEXT,
    logo_url VARCHAR(500),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address TEXT,
    date_started DATE,
    last_maintenance DATE,
    developer_company VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Insert default record (singleton)
INSERT INTO store_settings (store_name) VALUES ('Emjay Stores');
