package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Operations with identity roles - usable in wf
 * 
 * @author svanda
 *
 */
public interface IdmIdentityRoleService extends ReadWriteEntityService<IdmIdentityRole, QuickFilter> {
	
	/**
	 * Returns identity roles by their ids (uuid in string).
	 * 
	 * @param ids
	 * @return
	 */
	List<IdmIdentityRole> getByIds(List<String> ids);

	IdmIdentityRole updateByDto(UUID id, IdmIdentityRoleDto dto);

	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);
}
