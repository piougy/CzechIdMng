--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extended form projection validations

ALTER TABLE idm_form_projection ADD form_validations text NULL;
ALTER TABLE idm_form_projection_a ADD form_validations text NULL;
ALTER TABLE idm_form_projection_a ADD form_validations_m bool NULL;

