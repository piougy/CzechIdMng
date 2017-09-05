--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- change storage key for eav attrributest - code switched to uuid

-- system
update idm_confidential_storage set storage_key = (
	select 'eav:' || encode(a.id, 'hex')::uuid 
	from idm_form_attribute a, idm_form_definition d 
	where a.confidential = true and a.definition_id = d.id 
		and d.definition_type = 'eu.bcvsolutions.idm.acc.entity.SysSystem' and a.code = substring(storage_key from 5)
)
where storage_key like 'eav:%' and owner_type = 'eu.bcvsolutions.idm.acc.entity.SysSystemFormValue';




