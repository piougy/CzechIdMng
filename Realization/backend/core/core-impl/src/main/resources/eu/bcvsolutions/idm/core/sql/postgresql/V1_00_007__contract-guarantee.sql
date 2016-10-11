-- rename garant to guarantee
ALTER TABLE idm_identity_contract RENAME COLUMN garant_id TO guarantee_id;
ALTER TABLE idm_identity_contract_aud RENAME COLUMN garant_id TO guarantee_id;