--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- change storage key for eav attrributest - code switched to uuid

-- system
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, sys_system_form_value v
	where v.attribute_id = a.id and idm_confidential_storage.owner_id = v.id
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.acc.entity.SysSystemFormValue';




