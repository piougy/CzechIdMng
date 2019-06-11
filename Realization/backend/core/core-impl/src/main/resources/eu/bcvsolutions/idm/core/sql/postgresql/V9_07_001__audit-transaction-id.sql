--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add transaction id into global audit table

ALTER TABLE idm_audit ADD COLUMN transaction_id bytea;

CREATE INDEX idx_idm_audit_trans_id ON idm_audit USING btree (transaction_id);
CREATE INDEX idx_idm_entity_event_trans_id ON idm_entity_event USING btree (transaction_id);