--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add contrct position into concept role request

ALTER TABLE idm_concept_role_request ADD COLUMN contract_position_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN contract_position_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN contract_position_m bool;

CREATE INDEX idx_idm_conc_role_c_p ON idm_concept_role_request USING btree (contract_position_id);