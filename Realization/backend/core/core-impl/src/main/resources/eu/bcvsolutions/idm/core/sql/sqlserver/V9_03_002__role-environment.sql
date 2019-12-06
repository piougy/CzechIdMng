--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add role environment attribute

ALTER TABLE idm_role ADD environment nvarchar(255);
CREATE INDEX idx_idm_role_environment
  ON idm_role(environment);
ALTER TABLE idm_role_a ADD environment nvarchar(255);
ALTER TABLE idm_role_a ADD environment_m bit;

ALTER TABLE idm_role ADD base_code nvarchar(255)
GO
UPDATE idm_role SET base_code = code;

ALTER TABLE idm_role_a ADD base_code nvarchar(255);
ALTER TABLE idm_role_a ADD base_code_m bit;

ALTER TABLE idm_role ALTER COLUMN base_code nvarchar(255) NOT NULL;

CREATE INDEX idx_idm_role_base_code
  ON idm_role(base_code);