--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- add root event to event mechanism
-- add persistent super owner id to event and state

ALTER TABLE idm_entity_event ADD root_id binary(16);
CREATE INDEX idx_idm_entity_event_root
  ON idm_entity_event(root_id);
  
ALTER TABLE idm_entity_event ADD super_owner_id binary(16);
CREATE INDEX idx_idm_entity_event_so_id
  ON idm_entity_event(super_owner_id);
  
ALTER TABLE idm_entity_state ADD super_owner_id binary(16);
CREATE INDEX idx_idm_entity_state_so_id
  ON idm_entity_state(super_owner_id);
