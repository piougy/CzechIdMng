--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Add owner and sub owner column into idm audit

ALTER TABLE idm_audit ADD COLUMN owner_id  character varying(255);

ALTER TABLE idm_audit ADD COLUMN owner_code  character varying(255);

ALTER TABLE idm_audit ADD COLUMN owner_type  character varying(255);

ALTER TABLE idm_audit ADD COLUMN sub_owner_id  character varying(255);

ALTER TABLE idm_audit ADD COLUMN sub_owner_code  character varying(255);

ALTER TABLE idm_audit ADD COLUMN sub_owner_type  character varying(255);
