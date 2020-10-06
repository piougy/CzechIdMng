--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- entity event start and end date

ALTER TABLE idm_entity_event ADD COLUMN event_started timestamp without time zone;
ALTER TABLE idm_entity_event ADD COLUMN event_ended timestamp without time zone;

UPDATE idm_entity_event SET event_started = created;
UPDATE idm_entity_event SET event_ended = modified WHERE result_state <> 'CREATED' AND result_state <> 'RUNNING';

