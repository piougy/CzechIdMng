-- one of position has to be filled
UPDATE idm_identity_contract set position = 'default' where position is null;
ALTER TABLE idm_identity_contract
ADD CONSTRAINT c_idm_identity_contract_position
CHECK (
     (position IS NOT NULL) OR (working_position_id IS NOT NULL)
);