--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add system state to the role-request

ALTER TABLE idm_role_request
    ADD result_state nvarchar(45);
    
ALTER TABLE idm_role_request
    ADD result_model image;
    
ALTER TABLE idm_role_request
    ADD result_code nvarchar(255);
    
ALTER TABLE idm_role_request
    ADD result_cause nvarchar(MAX);

-- Add system state to the concept-role-request

ALTER TABLE idm_concept_role_request
    ADD result_state nvarchar(45);
    
ALTER TABLE idm_concept_role_request
    ADD result_model image;
    
ALTER TABLE idm_concept_role_request
    ADD result_code nvarchar(255);
    
ALTER TABLE idm_concept_role_request
    ADD result_cause nvarchar(MAX);