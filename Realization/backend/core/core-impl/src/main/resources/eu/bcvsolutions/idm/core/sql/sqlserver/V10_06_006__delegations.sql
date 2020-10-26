--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Delegations - convert varchar to nvarchar

DROP INDEX idx_i_del_owner_type ON idm_delegation;
DROP INDEX idx_i_del_def_type ON idm_delegation_def;

ALTER TABLE idm_delegation ALTER COLUMN creator nvarchar(255) NOT NULL;
ALTER TABLE idm_delegation ALTER COLUMN modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation ALTER COLUMN original_creator nvarchar(255) NULL;
ALTER TABLE idm_delegation ALTER COLUMN original_modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation ALTER COLUMN result_cause nvarchar(MAX) NULL;
ALTER TABLE idm_delegation ALTER COLUMN result_code nvarchar(255) NULL;
ALTER TABLE idm_delegation ALTER COLUMN result_state nvarchar(45) NULL;
ALTER TABLE idm_delegation ALTER COLUMN result_model image NULL;
ALTER TABLE idm_delegation ALTER COLUMN owner_type nvarchar(255) NOT NULL;

CREATE INDEX idx_i_del_owner_type ON idm_delegation (owner_type);

ALTER TABLE idm_delegation_a ALTER COLUMN creator nvarchar(255) NULL;
ALTER TABLE idm_delegation_a ALTER COLUMN modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_a ALTER COLUMN original_creator nvarchar(255) NULL;
ALTER TABLE idm_delegation_a ALTER COLUMN original_modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_a ALTER COLUMN owner_type nvarchar(255) NULL;

ALTER TABLE idm_delegation_def ALTER COLUMN creator nvarchar(255) NOT NULL;
ALTER TABLE idm_delegation_def ALTER COLUMN modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_def ALTER COLUMN original_creator nvarchar(255) NULL;
ALTER TABLE idm_delegation_def ALTER COLUMN original_modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_def ALTER COLUMN description nvarchar(2000) NULL;
ALTER TABLE idm_delegation_def ALTER COLUMN [type] nvarchar(255) NOT NULL;

CREATE INDEX idx_i_del_def_type ON idm_delegation_def ([type]);

ALTER TABLE idm_delegation_def_a ALTER COLUMN creator nvarchar(255) NULL;
ALTER TABLE idm_delegation_def_a ALTER COLUMN modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_def_a ALTER COLUMN original_creator nvarchar(255) NULL;
ALTER TABLE idm_delegation_def_a ALTER COLUMN original_modifier nvarchar(255) NULL;
ALTER TABLE idm_delegation_def_a ALTER COLUMN description nvarchar(2000) NULL;

ALTER TABLE idm_delegation_def_a ALTER COLUMN [type] nvarchar(255) NULL
GO

