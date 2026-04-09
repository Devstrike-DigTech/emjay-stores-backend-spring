CREATE TYPE conversation_category AS ENUM ('PRODUCT_REQUEST', 'STAFF_MESSAGE', 'CONTACT_US', 'INVENTORY', 'GENERAL');
CREATE TYPE participant_type AS ENUM ('CUSTOMER', 'STAFF', 'ADMIN');
CREATE TYPE message_type AS ENUM ('TEXT', 'IMAGE', 'PRODUCT_LINK', 'ORDER_LINK');

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category conversation_category NOT NULL DEFAULT 'GENERAL',
    subject VARCHAR(300),
    initiator_id UUID NOT NULL,
    initiator_type participant_type NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE conversation_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    sender_type participant_type NOT NULL,
    content TEXT NOT NULL,
    message_type message_type NOT NULL DEFAULT 'TEXT',
    reference_id UUID,
    sent_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_category ON conversations(category);
CREATE INDEX idx_conversations_is_read ON conversations(is_read);
CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at DESC);
CREATE INDEX idx_messages_conversation_id ON conversation_messages(conversation_id);
CREATE INDEX idx_messages_sent_at ON conversation_messages(sent_at DESC);
