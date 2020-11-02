--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- #2543 fix assigned automatic roles validity
update idm_identity_role set 
valid_from = (select ic.valid_from from idm_identity_contract ic where identity_contract_id = ic.id),
valid_till = (select ic.valid_till from idm_identity_contract ic where identity_contract_id = ic.id)
where automatic_role_id is not null;