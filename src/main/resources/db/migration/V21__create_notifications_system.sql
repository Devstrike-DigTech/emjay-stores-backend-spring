-- V21__create_notifications_system.sql
-- Notifications & Communications System (Email, SMS, Push)

-- ========== ENUMS ==========

CREATE TYPE notification_type AS ENUM (
    'ORDER_CONFIRMATION',
    'ORDER_SHIPPED',
    'ORDER_DELIVERED',
    'PAYMENT_RECEIVED',
    'PAYMENT_FAILED',
    'BOOKING_CONFIRMATION',
    'BOOKING_REMINDER',
    'BOOKING_CANCELLED',
    'BOOKING_RESCHEDULED',
    'BOOKING_COMPLETED',
    'PROMO_CODE',
    'ACCOUNT_CREATED',
    'PASSWORD_RESET',
    'CUSTOM'
);

CREATE TYPE notification_channel AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP');

CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'FAILED', 'RETRYING');

CREATE TYPE email_template_type AS ENUM (
    'ORDER_CONFIRMATION',
    'BOOKING_CONFIRMATION',
    'BOOKING_REMINDER',
    'PAYMENT_RECEIPT',
    'WELCOME_EMAIL',
    'PASSWORD_RESET',
    'PROMO_NOTIFICATION'
);

-- ========== EMAIL TEMPLATES ==========

CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL UNIQUE,
    template_type email_template_type NOT NULL,
    subject VARCHAR(300) NOT NULL,

    -- Template content (HTML + Plain Text)
    html_content TEXT NOT NULL,
    text_content TEXT,

    -- Template variables (JSON list)
    variables JSONB, -- e.g., ["customer_name", "order_number", "total_amount"]

    -- Metadata
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_templates_type ON email_templates(template_type);
CREATE INDEX idx_email_templates_active ON email_templates(is_active);

-- ========== SMS TEMPLATES ==========

CREATE TABLE sms_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL UNIQUE,
    notification_type notification_type NOT NULL,

    -- SMS content (max 160 chars recommended)
    content VARCHAR(500) NOT NULL,

    -- Template variables
    variables JSONB,

    -- Metadata
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sms_templates_type ON sms_templates(notification_type);

-- ========== NOTIFICATION QUEUE ==========

CREATE TABLE notification_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Recipient
    recipient_id UUID, -- Customer or User ID
    recipient_email VARCHAR(200),
    recipient_phone VARCHAR(20),
    recipient_name VARCHAR(200),

    -- Notification details
    notification_type notification_type NOT NULL,
    channel notification_channel NOT NULL,

    -- Content
    subject VARCHAR(300),
    message TEXT NOT NULL,
    html_content TEXT, -- For emails

    -- Template reference
    template_id UUID,
    template_data JSONB, -- Variables for template rendering

    -- Status & Tracking
    status notification_status NOT NULL DEFAULT 'PENDING',
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,

    -- References
    related_entity_type VARCHAR(50), -- ORDER, BOOKING, CUSTOMER
    related_entity_id UUID,

    -- Provider tracking
    provider_message_id VARCHAR(200), -- External provider's message ID

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_queue_status ON notification_queue(status);
CREATE INDEX idx_notification_queue_channel ON notification_queue(channel);
CREATE INDEX idx_notification_queue_scheduled ON notification_queue(scheduled_for);
CREATE INDEX idx_notification_queue_recipient ON notification_queue(recipient_id);
CREATE INDEX idx_notification_queue_type ON notification_queue(notification_type);

-- ========== NOTIFICATION HISTORY ==========

CREATE TABLE notification_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Original queue reference
    queue_id UUID REFERENCES notification_queue(id),

    -- Recipient
    recipient_id UUID,
    recipient_email VARCHAR(200),
    recipient_phone VARCHAR(20),

    -- Notification details
    notification_type notification_type NOT NULL,
    channel notification_channel NOT NULL,
    subject VARCHAR(300),
    message TEXT,

    -- Status
    status notification_status NOT NULL,
    sent_at TIMESTAMP,

    -- Provider info
    provider VARCHAR(50), -- SENDGRID, TWILIO, TERMII, etc.
    provider_message_id VARCHAR(200),
    provider_response JSONB,

    -- Metadata
    related_entity_type VARCHAR(50),
    related_entity_id UUID,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_history_recipient ON notification_history(recipient_id);
CREATE INDEX idx_notification_history_type ON notification_history(notification_type);
CREATE INDEX idx_notification_history_sent ON notification_history(sent_at);

-- ========== NOTIFICATION PREFERENCES ==========

CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,

    -- Email preferences
    email_order_updates BOOLEAN DEFAULT TRUE,
    email_booking_reminders BOOLEAN DEFAULT TRUE,
    email_promotions BOOLEAN DEFAULT TRUE,
    email_newsletter BOOLEAN DEFAULT TRUE,

    -- SMS preferences
    sms_order_updates BOOLEAN DEFAULT TRUE,
    sms_booking_reminders BOOLEAN DEFAULT TRUE,
    sms_promotions BOOLEAN DEFAULT FALSE,

    -- Push notification preferences
    push_order_updates BOOLEAN DEFAULT TRUE,
    push_booking_reminders BOOLEAN DEFAULT TRUE,
    push_promotions BOOLEAN DEFAULT TRUE,

    -- Global opt-out
    opt_out_all BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(customer_id)
);

CREATE INDEX idx_notification_prefs_customer ON notification_preferences(customer_id);

-- ========== SCHEDULED NOTIFICATIONS ==========

CREATE TABLE scheduled_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Schedule details
    notification_type notification_type NOT NULL,
    channel notification_channel NOT NULL,

    -- Trigger conditions
    trigger_type VARCHAR(50) NOT NULL, -- BOOKING_24H_BEFORE, ORDER_SHIPPED, etc.
    trigger_offset_hours INT, -- Hours before/after event

    -- Template
    template_id UUID,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scheduled_notifications_active ON scheduled_notifications(is_active);

-- ========== COMMENTS ==========

COMMENT ON TABLE notification_queue IS 'Queue for pending notifications to be sent';
COMMENT ON TABLE notification_history IS 'Historical record of sent notifications';
COMMENT ON TABLE email_templates IS 'Email templates with HTML content';
COMMENT ON TABLE sms_templates IS 'SMS message templates';
COMMENT ON TABLE notification_preferences IS 'Customer notification preferences';

COMMENT ON COLUMN notification_queue.template_data IS 'JSON data for template variable substitution';
COMMENT ON COLUMN notification_queue.scheduled_for IS 'When to send the notification (NULL = send immediately)';
COMMENT ON COLUMN notification_queue.retry_count IS 'Number of retry attempts';
COMMENT ON COLUMN scheduled_notifications.trigger_type IS 'Event that triggers the notification';