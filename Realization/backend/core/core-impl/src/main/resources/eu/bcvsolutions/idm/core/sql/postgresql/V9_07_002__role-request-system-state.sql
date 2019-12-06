--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add system state to the role-request

ALTER TABLE idm_role_request
    ADD COLUMN result_state character varying(45);
    
ALTER TABLE idm_role_request
    ADD COLUMN result_model bytea;
    
ALTER TABLE idm_role_request
    ADD COLUMN result_code character varying(255);
    
ALTER TABLE idm_role_request
    ADD COLUMN result_cause text;

-- Add system state to the concept-role-request

ALTER TABLE idm_concept_role_request
    ADD COLUMN result_state character varying(45);
    
ALTER TABLE idm_concept_role_request
    ADD COLUMN result_model bytea;
    
ALTER TABLE idm_concept_role_request
    ADD COLUMN result_code character varying(255);
    
ALTER TABLE idm_concept_role_request
    ADD COLUMN result_cause text;