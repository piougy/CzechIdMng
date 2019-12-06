--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Update unique index on acc identity account.

DROP INDEX ux_identity_account ON acc_identity_account
GO
CREATE UNIQUE INDEX ux_identity_account ON acc_identity_account (identity_id,account_id,role_system_id,identity_role_id) WHERE identity_role_id IS NOT NULL;
