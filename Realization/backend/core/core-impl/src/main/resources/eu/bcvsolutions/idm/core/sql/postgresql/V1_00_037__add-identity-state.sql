--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- new identity state - disabled will be used internally

ALTER TABLE idm_identity ADD COLUMN state character varying(45);
ALTER TABLE idm_identity_a ADD COLUMN state character varying(45);
ALTER TABLE idm_identity_a ADD COLUMN state_m boolean;

UPDATE idm_identity SET state = 'VALID' WHERE disabled = false;
UPDATE idm_identity SET state = 'DISABLED' WHERE disabled = true;

ALTER TABLE idm_identity ALTER COLUMN state SET NOT NULL;