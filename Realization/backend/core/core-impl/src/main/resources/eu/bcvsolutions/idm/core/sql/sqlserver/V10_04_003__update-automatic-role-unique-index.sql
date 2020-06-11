--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Update unique index for automatic role for contract and position
DROP INDEX ux_idm_identity_role_cont_aut ON idm_identity_role;
CREATE UNIQUE INDEX ux_idm_identity_role_cont_aut ON idm_identity_role (identity_contract_id,automatic_role_id) 
WHERE automatic_role_id IS NOT NULL and contract_position_id IS NULL;
