--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- role type - start usage + cleanup obsolete role types

ALTER TABLE idm_role ALTER COLUMN role_type DROP NOT NULL;
UPDATE idm_role SET role_type = null WHERE role_type = 'BUSINESS';
UPDATE idm_role SET role_type = null WHERE role_type = 'TECHNICAL';
UPDATE idm_role SET role_type = null WHERE role_type = 'LOGIN';
