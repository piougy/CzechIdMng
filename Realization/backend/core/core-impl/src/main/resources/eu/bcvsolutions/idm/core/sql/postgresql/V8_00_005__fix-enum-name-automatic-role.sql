--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- fix enum name for AutomaticRoleAttributeRuleType

UPDATE idm_auto_role_att_rule SET type = 'IDENTITY_EAV' where type = 'IDENITITY_EAV';
