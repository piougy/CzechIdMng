--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add role environment attribute
--
-- switch mapping to new code
--
UPDATE sys_role_system_attribute SET idm_property_name = 'baseCode' 
WHERE idm_property_name = 'code' and entity_attribute = true and system_attr_mapping_id IN 
(SELECT id FROM sys_system_attribute_mapping WHERE system_mapping_id IN (SELECT id FROM sys_system_mapping WHERE entity_type = 'ROLE'));
--
UPDATE sys_system_attribute_mapping SET idm_property_name = 'baseCode' 
WHERE idm_property_name = 'code' and entity_attribute = true and system_mapping_id IN (SELECT id FROM sys_system_mapping WHERE entity_type = 'ROLE');