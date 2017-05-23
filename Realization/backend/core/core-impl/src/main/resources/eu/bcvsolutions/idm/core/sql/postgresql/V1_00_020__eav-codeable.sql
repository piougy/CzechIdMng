--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script adds codeable interface to eav form definitions and attributes

-- form definition
ALTER TABLE idm_form_definition ADD COLUMN code character varying(255);
ALTER TABLE idm_form_definition ADD COLUMN main boolean NOT NULL DEFAULT false;
ALTER TABLE idm_form_definition ADD COLUMN description character varying(2000);
ALTER TABLE idm_form_definition DROP CONSTRAINT ux_idm_form_definition_tn;
ALTER TABLE idm_form_definition ADD CONSTRAINT ux_idm_form_definition_tn unique (definition_type,code);
UPDATE idm_form_definition SET code = name;
UPDATE idm_form_definition SET main = true WHERE code = 'default';
ALTER TABLE idm_form_definition ALTER COLUMN code SET NOT NULL;
-- audit
ALTER TABLE idm_form_definition_a ADD COLUMN code character varying(255);
ALTER TABLE idm_form_definition_a ADD COLUMN code_m boolean;
ALTER TABLE idm_form_definition_a ADD COLUMN main boolean;
ALTER TABLE idm_form_definition_a ADD COLUMN main_m boolean;
ALTER TABLE idm_form_definition_a ADD COLUMN description character varying(2000);
ALTER TABLE idm_form_definition_a ADD COLUMN description_m boolean;

--
-- form attribute
ALTER TABLE idm_form_attribute DROP CONSTRAINT ux_idm_f_a_definition_name;
ALTER TABLE idm_form_attribute RENAME name TO code ;
ALTER TABLE idm_form_attribute RENAME display_name TO name;
ALTER TABLE idm_form_attribute ADD CONSTRAINT ux_idm_f_a_definition_name unique (definition_id,code);
-- audit
ALTER TABLE idm_form_attribute_a RENAME name TO code;
ALTER TABLE idm_form_attribute_a RENAME display_name TO name;
ALTER TABLE idm_form_attribute_a RENAME name_m TO code_m;
ALTER TABLE idm_form_attribute_a RENAME display_name_m TO name_m;



