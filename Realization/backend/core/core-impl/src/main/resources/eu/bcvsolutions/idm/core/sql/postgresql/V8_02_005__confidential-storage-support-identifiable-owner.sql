--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Support Dto as owner in confidential storage
-- Owner type is entity every time

update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmContractSlice' 
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto';
update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract'
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto';
update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity' 
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto';
update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue' 
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto';
update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmRole'
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmRoleDto';
update idm_confidential_storage set owner_type = 'eu.bcvsolutions.idm.core.model.entity.IdmTreeNode'
	where owner_type = 'eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto';
