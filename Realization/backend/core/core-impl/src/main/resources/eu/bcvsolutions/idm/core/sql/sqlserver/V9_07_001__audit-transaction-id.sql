--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add transaction id into global audit table

ALTER TABLE idm_audit ADD transaction_id binary(16);

CREATE INDEX idx_idm_audit_trans_id ON idm_audit (transaction_id);
CREATE INDEX idx_idm_entity_event_trans_id ON idm_entity_event (transaction_id);