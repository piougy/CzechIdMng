--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- #2543 fix assigned automatic roles validity
update ir set 
valid_from = (select ic.valid_from from idm_identity_contract ic where ir.identity_contract_id = ic.id),
valid_till = (select ic.valid_till from idm_identity_contract ic where ir.identity_contract_id = ic.id)
from idm_identity_role ir
where ir.automatic_role_id is not null
or (ir.direct_role_id is not null and exists (select dr.id from idm_identity_role dr where dr.id = ir.direct_role_id and dr.automatic_role_id is not null));