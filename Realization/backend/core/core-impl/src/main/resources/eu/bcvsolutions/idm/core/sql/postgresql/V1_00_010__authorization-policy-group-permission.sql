--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add GroupPermission to authentication policy and remove original authorities

ALTER TABLE idm_authorization_policy ADD COLUMN group_permission character varying(255);
ALTER TABLE idm_authorization_policy_a ADD COLUMN group_permission character varying(255);
ALTER TABLE idm_authorization_policy_a ADD COLUMN group_permission_m boolean;
DROP TABLE idm_role_authority;
