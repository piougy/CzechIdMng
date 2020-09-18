--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- concept - add business role

ALTER TABLE idm_concept_role_request ADD direct_role_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD direct_role_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD direct_role_m bit;
ALTER TABLE idm_concept_role_request ADD direct_concept_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD direct_concept_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD direct_concept_m bit;
ALTER TABLE idm_concept_role_request ADD role_composition_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD role_composition_id binary(16);
ALTER TABLE idm_concept_role_request_a ADD role_composition_m bit;

