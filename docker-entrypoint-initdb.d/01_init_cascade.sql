ALTER TABLE pinned_messages DROP CONSTRAINT IF EXISTS fk_pinned_messages_message_id;

ALTER TABLE pinned_messages 
ADD CONSTRAINT fk_pinned_messages_message_id 
FOREIGN KEY (message_id) 
REFERENCES messages(id) 
ON DELETE CASCADE;