--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Update unique index for automatic role for contract and position
DROP INDEX ux_idm_identity_role_cont_aut;
CREATE UNIQUE INDEX ux_idm_identity_role_cont_aut ON idm_identity_role (identity_contract_id,contract_position_id,automatic_role_id);

