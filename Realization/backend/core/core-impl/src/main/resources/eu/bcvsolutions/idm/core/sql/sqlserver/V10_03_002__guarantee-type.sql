--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add role guarantee type attribute

-- type guarantee-identity
ALTER TABLE idm_role_guarantee ADD guarantee_type nvarchar(255);
CREATE INDEX idx_idm_role_guarantee_type
  ON idm_role_guarantee(guarantee_type);
ALTER TABLE idm_role_guarantee_a ADD guarantee_type nvarchar(255);
ALTER TABLE idm_role_guarantee_a ADD type_m bit;

-- type guarantee-role
ALTER TABLE idm_role_guarantee_role ADD guarantee_type nvarchar(255);
CREATE INDEX idx_idm_role_g_r_type
  ON idm_role_guarantee_role(guarantee_type);
ALTER TABLE idm_role_guarantee_role_a ADD guarantee_type nvarchar(255);
ALTER TABLE idm_role_guarantee_role_a ADD type_m bit;