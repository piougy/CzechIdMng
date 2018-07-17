--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Alter foregin keys for logging event exception and property

-- Drop
ALTER TABLE logging_event_exception
DROP CONSTRAINT logging_event_exception_event_id_fkey;

ALTER TABLE logging_event_property
DROP CONSTRAINT logging_event_property_event_id_fkey;

-- Create
ALTER TABLE logging_event_exception ADD CONSTRAINT
	logging_event_exception_event_id_fkey FOREIGN KEY (event_id) REFERENCES logging_event(event_id) ON DELETE CASCADE;

	ALTER TABLE logging_event_property ADD CONSTRAINT
	logging_event_property_event_id_fkey FOREIGN KEY (event_id) REFERENCES logging_event(event_id) ON DELETE CASCADE;
