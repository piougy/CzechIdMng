--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- concept - add business role

ALTER TABLE idm_concept_role_request ADD COLUMN direct_role_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN direct_role_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN direct_role_m bool;
ALTER TABLE idm_concept_role_request ADD COLUMN direct_concept_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN direct_concept_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN direct_concept_m bool;
ALTER TABLE idm_concept_role_request ADD COLUMN role_composition_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN role_composition_id bytea;
ALTER TABLE idm_concept_role_request_a ADD COLUMN role_composition_m bool;

