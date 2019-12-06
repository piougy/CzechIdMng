--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add contrct position into concept role request

ALTER TABLE idm_concept_role_request ADD contract_position_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD contract_position_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD contract_position_m bit;

CREATE INDEX idx_idm_conc_role_c_p ON idm_concept_role_request (contract_position_id) ;