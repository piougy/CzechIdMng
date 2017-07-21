--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add indexes to logging_event and logging_event_exception

CREATE INDEX idx_log_event_timestmp
  ON logging_event
  USING btree
  (timestmp);

CREATE INDEX idx_log_event_logger_name
  ON logging_event
  USING btree
  (logger_name);

CREATE INDEX idx_log_event_caller_filename
  ON logging_event
  USING btree
  (caller_filename);

CREATE INDEX idx_log_event_caller_class
  ON logging_event
  USING btree
  (caller_class);

CREATE INDEX idx_log_event_caller_method
  ON logging_event
  USING btree
  (caller_method);

CREATE INDEX idx_log_event_ex_event_id
  ON logging_event_exception
  USING btree
  (event_id);
