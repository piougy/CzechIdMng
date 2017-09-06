--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- change storage key for eav attrributest - code switched to uuid

-- identity
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, idm_identity_form_value v
	where v.attribute_id = a.id and idm_confidential_storage.owner_id = v.id
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue';
-- role
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, idm_role_form_value v
	where v.attribute_id = a.id and idm_confidential_storage.owner_id = v.id
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue';
-- contract
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, idm_i_contract_form_value v
	where v.attribute_id = a.id and idm_confidential_storage.owner_id = v.id
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue';
-- tree
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, idm_tree_node_form_value v
	where v.attribute_id = a.id and idm_confidential_storage.owner_id = v.id
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue';




