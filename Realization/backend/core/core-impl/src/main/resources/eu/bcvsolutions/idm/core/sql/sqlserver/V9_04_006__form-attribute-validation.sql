--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add form attribute validation fields

ALTER TABLE idm_form_attribute ADD validation_max numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD validation_max numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD max_m bit;

ALTER TABLE idm_form_attribute ADD validation_min numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD validation_min numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD min_m bit;

ALTER TABLE idm_form_attribute ADD validation_regex nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD validation_regex nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD regex_m bit;

ALTER TABLE idm_form_attribute ADD validation_unique bit NOT NULL DEFAULT 0;
ALTER TABLE idm_form_attribute_a ADD validation_unique bit;
ALTER TABLE idm_form_attribute_a ADD unique_m bit;

ALTER TABLE idm_form_attribute ADD validation_message nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD validation_message nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD validation_message_m bit;

ALTER TABLE idm_role_form_attribute ADD validation_message nvarchar(2000);
ALTER TABLE idm_role_form_attribute_a ADD validation_message nvarchar(2000);
ALTER TABLE idm_role_form_attribute_a ADD validation_message_m bit;
