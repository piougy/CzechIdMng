--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add index to formatted_message, table logging_event


ALTER TABLE logging_event ADD COLUMN formatted_message_new text;


CREATE INDEX idx_log_event_formatted_message
  ON logging_event
  USING btree (formatted_message_new);


UPDATE logging_event SET formatted_message_new = formatted_message;


ALTER TABLE logging_event DROP COLUMN formatted_message;


ALTER TABLE logging_event RENAME COLUMN formatted_message_new TO formatted_message;


ALTER TABLE logging_event ALTER COLUMN formatted_message set NOT NULL;

