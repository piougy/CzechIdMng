--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add module indentifier for the form definition

ALTER TABLE idm_form_definition ADD module_id nvarchar(255);
ALTER TABLE idm_form_definition_a ADD module_id nvarchar(255);
ALTER TABLE idm_form_definition_a ADD module_m bit
GO
UPDATE idm_form_definition SET module_id = 'core' WHERE definition_type LIKE 'eu.bcvsolutions.idm.core.%';
UPDATE idm_form_definition SET module_id = 'acc' WHERE definition_type LIKE 'eu.bcvsolutions.idm.acc.%';
UPDATE idm_form_definition SET module_id = 'vs' WHERE definition_type LIKE 'eu.bcvsolutions.idm.vs.%';
UPDATE idm_form_definition SET module_id = 'ic' WHERE definition_type LIKE 'eu.bcvsolutions.idm.ic.%';
UPDATE idm_form_definition SET module_id = 'crt' WHERE definition_type LIKE 'eu.bcvsolutions.idm.crt.%';
UPDATE idm_form_definition SET module_id = 'rpt' WHERE definition_type LIKE 'eu.bcvsolutions.idm.rpt.%';


