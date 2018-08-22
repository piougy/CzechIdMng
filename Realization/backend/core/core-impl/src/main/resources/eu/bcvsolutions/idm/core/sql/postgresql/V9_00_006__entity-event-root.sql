--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- add root event to event mechanism
-- add persistent super owner id to event and state

ALTER TABLE idm_entity_event ADD COLUMN root_id bytea;
CREATE INDEX idx_idm_entity_event_root
  ON idm_entity_event
  USING btree
  (root_id);
  
ALTER TABLE idm_entity_event ADD COLUMN super_owner_id bytea;
CREATE INDEX idx_idm_entity_event_so_id
  ON idm_entity_event
  USING btree
  (super_owner_id);
  
ALTER TABLE idm_entity_state ADD COLUMN super_owner_id bytea;
CREATE INDEX idx_idm_entity_state_so_id
  ON idm_entity_state
  USING btree
  (super_owner_id);
