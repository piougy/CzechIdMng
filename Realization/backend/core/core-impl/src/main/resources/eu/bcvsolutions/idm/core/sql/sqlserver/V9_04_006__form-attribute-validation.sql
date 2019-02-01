--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add form attribute validation fields

ALTER TABLE idm_form_attribute ADD COLUMN validation_max numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_max numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD COLUMN max_m bit;

ALTER TABLE idm_form_attribute ADD COLUMN validation_min numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_min numeric(38,4);
ALTER TABLE idm_form_attribute_a ADD COLUMN min_m bit;

ALTER TABLE idm_form_attribute ADD COLUMN validation_regex nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_regex nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD COLUMN regex_m bit;

ALTER TABLE idm_form_attribute ADD COLUMN validation_unique bit NOT NULL DEFAULT false;
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_unique bit;
ALTER TABLE idm_form_attribute_a ADD COLUMN unique_m bit;

ALTER TABLE idm_form_attribute ADD COLUMN validation_message nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_message nvarchar(2000);
ALTER TABLE idm_form_attribute_a ADD COLUMN validation_message_m bit;

ALTER TABLE idm_role_form_attribute ADD COLUMN validation_message nvarchar(2000);
ALTER TABLE idm_role_form_attribute_a ADD COLUMN validation_message nvarchar(2000);
ALTER TABLE idm_role_form_attribute_a ADD COLUMN validation_message_m bit;
