--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add unique index to automaticaly assigned role.

CREATE UNIQUE INDEX IF NOT EXISTS ux_idm_identity_role_cont_aut ON idm_identity_role (identity_contract_id,automatic_role_id);