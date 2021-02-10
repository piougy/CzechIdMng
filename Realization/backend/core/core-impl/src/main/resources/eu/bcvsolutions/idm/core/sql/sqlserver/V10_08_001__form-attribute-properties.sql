--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- form attribute properties

ALTER TABLE idm_form_attribute ADD face_properties image NULL;
ALTER TABLE idm_form_attribute_a ADD face_properties image NULL;
ALTER TABLE idm_form_attribute_a ADD properties_m bit NULL;

