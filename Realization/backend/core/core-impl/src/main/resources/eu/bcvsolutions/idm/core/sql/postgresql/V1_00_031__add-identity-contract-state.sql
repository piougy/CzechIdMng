--
-- CzechIdM 7.5 Flyway script 
-- BCV solutions s.r.o.
--
-- add identity contract state

ALTER TABLE idm_identity_contract ADD COLUMN state character varying(45);
ALTER TABLE idm_identity_contract_a ADD COLUMN state character varying(45);
ALTER TABLE idm_identity_contract_a ADD COLUMN state_m boolean;

UPDATE idm_identity_contract SET state = 'EXCLUDED', disabled = false WHERE
	(valid_from is null OR valid_from <= now()) 
	AND (valid_till is null or valid_till >= now()) 
	AND disabled = true;